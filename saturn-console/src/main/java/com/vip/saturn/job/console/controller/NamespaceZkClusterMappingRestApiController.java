package com.vip.saturn.job.console.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.vip.saturn.job.console.exception.SaturnJobConsoleException;
import com.vip.saturn.job.console.exception.SaturnJobConsoleHttpException;
import com.vip.saturn.job.console.mybatis.entity.NamespaceZkClusterMapping;
import com.vip.saturn.job.console.mybatis.entity.ZkClusterInfo;
import com.vip.saturn.job.console.mybatis.service.NamespaceZkClusterMappingService;
import com.vip.saturn.job.console.mybatis.service.ZkClusterInfoService;

/**
 * 
 * @author hebelala
 *
 */
@Controller
@RequestMapping("/rest/v1")
public class NamespaceZkClusterMappingRestApiController {

	public final static String BAD_REQ_MSG_PREFIX = "Invalid request.";

	public final static String MISSING_REQUEST_MSG = BAD_REQ_MSG_PREFIX + " Missing parameter: {%s}";

	@Resource
	private ZkClusterInfoService zkClusterInfoService;

	@Resource
	private NamespaceZkClusterMappingService namespaceZkclusterMappingService;

	@RequestMapping(value = "/discoverZk", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<Object> discoverZk(String namespace, HttpServletRequest request)
			throws SaturnJobConsoleException {
		HttpHeaders headers = new HttpHeaders();
		try {
			if (StringUtils.isBlank(namespace)) {
				throw new SaturnJobConsoleHttpException(HttpStatus.BAD_REQUEST.value(),
						String.format(MISSING_REQUEST_MSG, "namespace"));
			}

			NamespaceZkClusterMapping namespaceZkClusterMapping = namespaceZkclusterMappingService
					.getByNamespace(namespace);

			if (namespaceZkClusterMapping == null) {
				throw new SaturnJobConsoleHttpException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
						"The NamespaceZkClusterMapping is not configured in db for " + namespace);
			}
			String clusterKey = namespaceZkClusterMapping.getClusterKey();
			ZkClusterInfo zkClusterInfo = zkClusterInfoService.getByClusterKey(clusterKey);
			if (zkClusterInfo == null) {
				throw new SaturnJobConsoleHttpException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
						"The clusterKey(" + clusterKey + ") is not configured in db for " + namespace);
			}
			return new ResponseEntity<Object>(zkClusterInfo.getConnectString(), headers, HttpStatus.OK);
		} catch (SaturnJobConsoleException e) {
			throw e;
		} catch (Exception e) {
			throw new SaturnJobConsoleHttpException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), e);
		}
	}

}
