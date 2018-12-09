package io.lundie.michael.viewcue.utilities;

/**
 * Enum providing various status variables used in conjunction with data status observables
 * Probably over-kill on the stats here
 */
public enum DataStatus {
    ATTEMPTING_API_FETCH,
    FETCH_COMPLETE,
    FETCHING_FROM_DATABASE,
    DATABASE_EMPTY,
    NO_DATA_AVAILABLE,
    ERROR_PARSING,
    ERROR_NETWORK_FAILURE,
    ERROR_UNAVAILABLE_OFFLINE,
    ERROR_NOT_FOUND,
    ERROR_SERVER_BROKEN,
    ERROR_UNKNOWN
}