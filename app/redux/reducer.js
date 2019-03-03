import {REDUX_ACTION} from '../constants';
import {combinedReducer} from 'redux';

const initialUCState = {fetching: true, useCases: []};
const initialUCResultState = {fetching: false, result: []};
const initialExecutionsState = {fetching: true, execs: []};
const initialModalState = {open: false, data: []};

function retrieveAll(state = initialUCState, action) {
    switch (action.type) {
        case REDUX_ACTION.RETRIEVE_ALL:
            return Object.assign({}, state, {fetching: action.fetching, useCases: action.useCases});
        default:
            return state;
    }
}

function ucExecResult(state = initialUCResultState, action) {
    switch (action.type) {
        case REDUX_ACTION.UC_RESULT:
            return Object.assign({}, state, {fetching: action.fetching, result: action.result});
        default:
            return state;
    }
}

function executions(state = initialExecutionsState, action) {
    switch (action.type) {
        case REDUX_ACTION.ALL_EXECUTIONS:
            return Object.assign({}, state, {fetching: action.fetching, execs: action.execs});
        default:
            return state;
    }
}

function launchComparatorModal(state = initialModalState, action) {
    switch (action.type) {
        case REDUX_ACTION.LAUNCH_MODAL:
            return Object.assign({}, state, {open: action.open, data: action.data});
        default:
            return state;
    }
}

export default combinedReducer({})