/**
 * 
 */
package org.iman.Heimdallr.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.iman.Heimdallr.config.annotations.JsonParam;
import org.iman.Heimdallr.constants.enums.ErrorCode;
import org.iman.Heimdallr.constants.enums.ResultCheckMode;
import org.iman.Heimdallr.entity.ApiDeclaration;
import org.iman.Heimdallr.entity.ApiTestCase;
import org.iman.Heimdallr.entity.DBConnectionInfo;
import org.iman.Heimdallr.exception.DataConversionException;
import org.iman.Heimdallr.service.APIService;
import org.iman.Heimdallr.service.ApiTestCaseService;
import org.iman.Heimdallr.service.DBConnectionInfoService;
import org.iman.Heimdallr.utils.BeanUtils;
import org.iman.Heimdallr.utils.ControllerUtils;
import org.iman.Heimdallr.vo.ApiTestCaseVo;
import org.iman.Heimdallr.vo.PageInfo;
import org.iman.Heimdallr.vo.Pagination;
import org.iman.Heimdallr.vo.Response;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author ey
 *
 */
@RestController
@RequestMapping("/apiTest")
public class ApiTestController {

    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private APIService apiService;
    @Resource
    private DBConnectionInfoService dbConnectionInfoService;
    
    @PostMapping("saveApiTest")
    public Response<ApiTestCaseVo> saveApiTest(@RequestBody ApiTestCaseVo vo) {
        Response<ApiTestCaseVo> resp = new Response<ApiTestCaseVo>();
        System.out.println("SAVE API TEST: " + vo.toString());
        try {
           ApiTestCase testCase = apiTestCaseService.save(vo);
           Optional<ApiTestCaseVo> voRs = BeanUtils.copy(testCase, ApiTestCaseVo.class);
           resp.setData(voRs.get());
           
        } catch (DataConversionException e) {
            resp = ControllerUtils.encapsulateErrCode(ErrorCode.DATA_CONVERSION_FAILURE);
        }
        
        return resp;
    }
    
    @PostMapping("getApiTestCaseByGeneralCaseId")
    public Response<List<ApiTestCaseVo>> getApiTestCaseByGeneralCaseId(
            @JsonParam ApiTestCaseVo params, @JsonParam PageInfo pageInfo) {
        Response<List<ApiTestCaseVo>> resp = new Response<List<ApiTestCaseVo>>();
        
        try {
            Pagination<ApiTestCase> cases = apiTestCaseService.getByParams(params,
                    pageInfo.toPage());
            List<ApiTestCaseVo> vos = new ArrayList<ApiTestCaseVo>();
            if (!CollectionUtils.sizeIsEmpty(cases.getList())) {
                Iterator<ApiTestCase> it = cases.getList().iterator();
                while (it.hasNext()) {
                    ApiTestCase testCase = (ApiTestCase) it.next();
                    ApiTestCaseVo cpObj = packageVo(testCase);
                    vos.add(cpObj);
                }
            }
            
            resp.setData(vos);
            resp.setTotal(cases.getTotal());
            resp.setCurrent(cases.getCurrent());
            resp.setPageSize(cases.getPageSize());
        } catch (DataConversionException e) {
            resp = ControllerUtils.encapsulateErrCode(ErrorCode.DATA_CONVERSION_FAILURE);
        }
        
        return resp;
    }
    
    private ApiTestCaseVo packageVo(ApiTestCase tc) throws DataConversionException {
        ApiTestCaseVo cpObj = BeanUtils.copy(tc, ApiTestCaseVo.class).get();
        Optional<ApiDeclaration> api = apiService.getById(tc.getApiId());
        if(api.isPresent()) {
            cpObj.setApiName(api.get().getName());
        } else {
          //TODO: handle the api is not found  
        }
        
        if (ResultCheckMode.DB_DATA.equals(tc.getResultCheckMode())) {
            Optional<DBConnectionInfo> connInfo = dbConnectionInfoService.getById(tc.getDbConnId());
            if (connInfo.isPresent()) {
                cpObj.setDbConnName(connInfo.get().getName());
            } else {
              //TODO: handle the connInfo is not found  
            }
        }
        
        return cpObj;
    }
}
