package com.iris.increff.controller;

import com.iris.increff.model.AlgorithmParameters;
import com.iris.increff.model.AlgoParametersData;
import com.iris.increff.service.AlgorithmParametersService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api
@RestController
public class AlgoParametersController {

    @Autowired
    private AlgorithmParametersService algorithmParametersService;

    @ApiOperation(value = "Gets all AlgoParameters")
    @RequestMapping(path = "/api/algo", method = RequestMethod.GET)
    public List<AlgorithmParameters> getAll() {
        return algorithmParametersService.getAll();
    }

    @ApiOperation(value = "Gets a single AlgoParameter by its name")
    @RequestMapping(path = "/api/algo/{parameterSet}", method = RequestMethod.GET)
    public AlgorithmParameters get(@PathVariable String parameterSet) {
        return algorithmParametersService.get(parameterSet);
    }

    @ApiOperation(value = "Adds an AlgoParameter")
    @RequestMapping(path = "/api/algo", method = RequestMethod.POST)
    public void add(@RequestBody AlgorithmParameters algoParametersData) {
        algorithmParametersService.add(algoParametersData);
    }

    @ApiOperation(value = "Updates an AlgoParameter")
    @RequestMapping(path = "/api/algo/{parameterSet}", method = RequestMethod.PUT)
    public void update(@PathVariable String parameterSet, @RequestBody AlgorithmParameters algoParametersData) {
        algorithmParametersService.update(parameterSet, algoParametersData);
    }
}
