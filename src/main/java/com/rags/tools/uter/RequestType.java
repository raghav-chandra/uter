package com.rags.tools.uter;

/**
 * Created by ragha on 22-04-2018.
 */
public enum RequestType {
    CREATE_UC,
    CREATE_UCS,

    FETCH_UC,
    FETCH_UCS,

    EXECUTE_UC,
    EXECUTE_UCS,

    MATCH_RESULTS,

    GET_ALL_UC,
    GET_ALL_EXECUTIONS,

    MATCH_ARRAY,
    MATCH_ARRAY_ELEMENT,

    START_EXECUTION,
    FINISH_EXECUTION,
    FAIL_EXECUTION

}
