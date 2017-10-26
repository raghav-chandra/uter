export function persist(key, data) {
	localStorage.setItem(key, JSON.stringify(data));
}
export function retrieve(key) {
	return localStorage.getItem(key);
}
