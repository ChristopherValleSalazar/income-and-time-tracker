const BASE_URL = "http://raspberrypi:8080"; //testing url

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

export async function getAllRows(page = 0, size = 10) {
    return getJSON(`/api/amzTransaction/getAllRows?page=${page}&size=${size}`);
}

export async function saveAmzTable(data) {
    return postJSON("/api/amzTransaction/saveTable", data);
}

export async function getWorkerSummary(page = 0, size = 10) {
    return getJSON(`/api/amzTransaction/getWeeklyTotalsPerPerson?page=${page}&size=${size}`);
}

export async function getMonthlySummary(page = 0, size = 10) {
    return getJSON(`/api/amzTransaction/getAllTotalPerWeek?page=${page}&size=${size}`);
}

export async function deleteAmzRowById(id) {
    return request("url", {
        method: "DELETE",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ id }),
    });
}
