import {USER_ACTION} from './constants'
import {persist} from './localStorage'
const CALL_MAPPER={};
CALL_MAPPER[USER_ACTION.SAVE_UC]=saveUseCase;

export function execute(action, param) {
	return CALL_MAPPER[action](param);
}

function saveUseCase(param) {
	persist('charag',param);
}
