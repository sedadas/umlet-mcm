const NODE_ENV = import.meta.env.VITE_NODE_ENV;
const IS_DEV = NODE_ENV === 'development';
const API_PORT = import.meta.env.VITE_API_PORT;
const API_URL = import.meta.env.VITE_API_URL;

console.log('VITE_NODE_ENV:', NODE_ENV);

console.log(`Using API at ${API_URL}:${API_PORT}`)

if (!NODE_ENV || !API_PORT || !API_URL) {
    throw new Error('Environment variables VITE_NODE_ENV, VITE_API_URL and VITE_API_PORT are required.');
}


export const AppConfig = {
    projectName: "UMLetino MCM",
    version: "v1.0.0",
    // When the server is started through electron app (not in dev mode), 
    // base url needs to be specified explicitly (otherwise it uses file:// as a base url). In other cases, it is empty.
    apiBaseUrl: `${API_URL}:${API_PORT}`,
}
