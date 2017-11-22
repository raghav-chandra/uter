import {USER_ACTION} from './constants'
import {persist,retrieve} from './localStorage'

const CALL_MAPPER={};
CALL_MAPPER[USER_ACTION.SAVE_UC]=saveUseCase;
CALL_MAPPER[USER_ACTION.RETRIEVE_UC]=retrieveUseCase;

export function execute(action, param) {
	return CALL_MAPPER[action](param);
}

function saveUseCase(param) {
	persist('charag',param);
}

function retrieveUseCase(param) {
	let uc1=JSON.parse(retrieve('charag'));
	uc1.id='charag';
	let uc2=JSON.parse(retrieve('raghav'));
	uc2.id='raghav';
	return [uc1,uc2];
}
