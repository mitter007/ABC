package com.abc.errorcode;

import java.util.List;

public class AlarmResult {
    public String syscode;
    public String groupcode;
    public String firstDim;

    public long windowStart;
    public long windowEnd;

    public List<SecDimDetail> secDimDetails;
}
