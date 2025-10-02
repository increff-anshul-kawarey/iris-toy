package com.iris.increff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TsvProperties {

    @Value("${tsv.headers.styles}")
    private String[] stylesHeaders;

    @Value("${tsv.headers.sku}")
    private String[] skuHeaders;

    @Value("${tsv.headers.store}")
    private String[] storeHeaders;

    @Value("${tsv.headers.sales}")
    private String[] salesHeaders;

    @Value("${tsv.headers.pricebucket}")
    private String[] priceBucketHeaders;

    public String[] getStylesHeaders() {
        return stylesHeaders;
    }

    public String[] getSkuHeaders() {
        return skuHeaders;
    }

    public String[] getStoreHeaders() {
        return storeHeaders;
    }

    public String[] getSalesHeaders() {
        return salesHeaders;
    }

    public String[] getPriceBucketHeaders() {
        return priceBucketHeaders;
    }
}
