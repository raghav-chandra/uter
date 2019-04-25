import {USER_ACTION} from './constants';
import {executions, retrieveAll, ucExecResult} from './redux/action';
import {persist,retrieve} from './localStorage';

const CALL_MAPPER = {
    [USER_ACTION.SAVE_UC] : (dispatch, action, param, data) => executePostRequest(dispatch, 'futor/uc/create', data, (json) =>{
        alert(json);
        return () => {};
    }),

    [USER_ACTION.SAVE_UCS] : (dispatch, action, param, ucIds) => executePostRequest(dispatch, 'futor/ucs/create', ucIds, (json) =>{
        alert(json);
        return () => {};
    }),

    [USER_ACTION.RETRIEVE_ALL] : (dispatch, action, param, data) => executeGetRequest(dispatch, 'futor/all', retrieveAll),
    [USER_ACTION.RETRIEVE_ALL_EXECUTIONS] : (dispatch, action, param, data) => executeGetRequest(dispatch, 'futor/executions/all', executions),
    [USER_ACTION.EXECUTE_UC] : (dispatch, action, param, data) => {
        dispatch(ucExecResult());
        executeGetRequest(dispatch, 'futor/execute/uc/' + param, ucExecResult);
    },
    [USER_ACTION.LOOKUP_USECASES] : (dispatch, action, param, data) => executePostRequest(dispatch, 'futor/uc/lookup', data, retrieveAll),
};


const HTTP_GET ='GET';
const HTTP_POST ='POST';

export function executeRequest(action, param, data = null) {
    try {
        return CALL_MAPPER[action](dispatch, action, param, data);
    } catch (e) {
        alert ('Failed while ' + action + '. Please retry');
    }
}


function executePostRequest (dispatch, url, data, successAction) {
    executeRequest(dispatch, url, HTTP_POST, successAction, data);
}

function executeGetRequest (dispatch, url, successAction) {
    executeRequest(dispatch, url, HTTP_GET, successAction);
}

function executeRequest (dispatch, url, requestType, successAction, data) {
    const createPromise = (requestType) =>{
        if(requestType === HTTP_GET) {
            return fetch(url, {credentials: 'include'});
        } else {
            let req = new Request(url, {
                method:'POST',
                credentials:'include',
                headers: {'Content-Type' : 'application/json'},
                body: data !=null && typeof data === 'object' ? JSON.stringify(data) : data
             });
             return fetch(req);
        }
    }

    return createPromise(requestType)
        .then(response=>{
            if(!response.ok) {
                throw response.statusText;
            } else {
                return response.json();
            }
        }).then (json=>{
            if(json.success) {
                return dispatch (successAction(json.data));
            } else {
                throw json.message;
            }
        }).catch(function(error) {
                alert('Failed : ' + (error.message? error.message:error));
        });
}
