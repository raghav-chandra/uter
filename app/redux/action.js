import {REDUX_ACTION} from '../constants';

export function retrieveAll(useCases, fetching = false) {
    return {
        type: REDUX_ACTION.RETRIEVE_ALL,
        fetching,
        useCases
    }
}

export function ucExecResult(result, fetching = false) {
    return {
        type: REDUX_ACTION.UC_RESULT,
        fetching,
        result
    }
}

export function executions(execs, fetching = false) {
    return {
        type: REDUX_ACTION.ALL_EXECUTIONS,
        fetching,
        execs
    }
}

export function launchComparatorModal(open = false, data) {
    return {
        type: REDUX_ACTION.LAUNCH_MODAL,
        open,
        data
    }
}