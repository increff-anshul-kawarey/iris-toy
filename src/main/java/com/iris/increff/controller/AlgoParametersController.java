package com.iris.increff.controller;

import com.iris.increff.model.AlgoParametersData;
import com.iris.increff.util.ApiException;
import com.iris.increff.util.TempDataCreator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api
@RestController
public class AlgoParametersController {


    @ApiOperation(value = "Gets list of parameters")
    @RequestMapping(path = "/api/algo", method = RequestMethod.GET)
    public AlgoParametersData getAll() {
        System.out.println("Fetch Algo Parameter  Successfully");
        return TempDataCreator.getAlgoParameters();
    }

    @ApiOperation(value = "update parameter")
    @RequestMapping(path = "/api/algo", method = RequestMethod.POST)
    public AlgoParametersData addParams(@RequestBody AlgoParametersData algoParametersData) throws ApiException {
        System.out.println("Algo Parameter Updated Successfully");
        System.out.println(algoParametersData.getParameter1());
        System.out.println(algoParametersData.getParameter2());
        System.out.println(algoParametersData.getParameter3());
        System.out.println(algoParametersData.getParameter4());
        System.out.println(algoParametersData.getParameter5());
        return algoParametersData;

    }


}
