package com.atguigu.es.test.chat0929;

public class BaseEntity {
    private String syscode;
    private String groupCode;
    private String dimcode;
    private String dimcont;

    // 构造函数、getter、setter、toString()
    public BaseEntity(String syscode, String groupCode, String dimcode, String dimcont) {
        this.syscode = syscode;
        this.groupCode = groupCode;
        this.dimcode = dimcode;
        this.dimcont = dimcont;
    }

    public String getSyscode() {
        return syscode;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public String getDimcode() {
        return dimcode;
    }

    public String getDimcont() {
        return dimcont;
    }
}