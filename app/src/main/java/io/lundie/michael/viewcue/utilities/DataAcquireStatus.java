package io.lundie.michael.viewcue.utilities;

public enum DataAcquireStatus {
    FETCH_COMPLETE,
    FETCHING_FROM_DATABASE,
    ERROR_PARSING,
    ERROR_NETWORK_FAILURE,
    ERROR_NOT_FOUND,
    ERROR_SERVER_BROKEN,
    ERROR_UNKNOWN
}