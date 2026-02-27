const BASE_URL = "http://192.168.68.63:8080";

async function request(endpoint, options = {}) {
    const url = `${BASE_URL}${endpoint}`;

    try {
        const response = await fetch(url, options);

        if (!response.ok) {
            throw new Error(`${options.method || "GET"} ${endpoint} failed: ${response.status} ${response.statusText}`);
        }

        return response;
    } catch (error) {
        console.error(`API error – ${url}:`, error);
        throw error;
    }
}

async function getJSON(endpoint) {
    const response = await request(endpoint);
    return response.json();
}

async function postJSON(endpoint, body) {
    return request(endpoint, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
    });
}


export async function loadTable() {
    return getJSON("/api/table/loadTable");
}

export async function saveTable(data) {
    return postJSON("/api/table/saveTable", data);
}

export async function getTotalHours() {
    return getJSON("/api/table/getTotalHours");
}

export async function getAllWorkerNames() {
    return getJSON("/api/amzTransaction/getAllWorkerNames");
}

export async function getAllRows() {
    return getJSON("/api/amzTransaction/getAllRows");
}

export async function saveAmzTable(data) {
    return postJSON("/api/amzTransaction/saveTable", data);
}

export async function getWorkerSummary() {
    return getJSON("/api/amzTransaction/getWorkerSummary");
}

export async function getMonthlySummary() {
    return getJSON("/api/amzTransaction/getAllTotalPerWeek");
}
