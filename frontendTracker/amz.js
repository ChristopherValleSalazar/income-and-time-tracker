import * as api from "./api.js";
import "./theme.js";

const PAGE_SIZE = 7;
const paginationState = {
    amzTable: { currentPage: 0, pageSize: PAGE_SIZE, totalPages: 1 },
    workerSummaryTable: { currentPage: 0, pageSize: PAGE_SIZE, totalPages: 1 },
    weeklySummaryTable: { currentPage: 0, pageSize: PAGE_SIZE, totalPages: 1 },
};

const cachedRows = {
    amzTable: [],
    workerSummaryTable: [],
    weeklySummaryTable: [],
};

function syncPaginationState(tableId, pagePayload) {
    const state = paginationState[tableId];
    state.currentPage = pagePayload.number;
    state.pageSize = pagePayload.size;
    state.totalPages = Math.max(1, pagePayload.totalPages || 1);
}

function buildPageButton({ label, className, disabled = false, active = false, onClick }) {
    const button = document.createElement("button");
    button.type = "button";
    button.textContent = label;
    button.className = className;
    button.disabled = disabled;

    if (active) {
        button.classList.add("active");
        button.setAttribute("aria-current", "page");
    }

    if (onClick) {
        button.addEventListener("click", onClick);
    }

    return button;
}

function getVisiblePageNumbers(totalPages, currentPage) {
    const maxButtons = 5;
    const currentDisplayPage = currentPage + 1;

    if (totalPages <= maxButtons) {
        return Array.from({ length: totalPages }, (_, i) => i);
    }

    let startDisplay = Math.max(1, currentDisplayPage - 2);
    let endDisplay = Math.min(totalPages, startDisplay + maxButtons - 1);

    if ((endDisplay - startDisplay + 1) < maxButtons) {
        startDisplay = Math.max(1, endDisplay - maxButtons + 1);
    }

    return Array.from({ length: endDisplay - startDisplay + 1 }, (_, i) => (startDisplay - 1) + i);
}

function renderPaginationControls(tableId, onPageChange) {
    const container = document.querySelector(`.pagination-bubble[data-table="${tableId}"]`);
    if (!container) {
        return;
    }

    const state = paginationState[tableId];
    container.replaceChildren();

    const prevButton = buildPageButton({
        label: "Prev",
        className: "page-bubble page-nav",
        disabled: state.currentPage === 0,
        onClick: () => {
            onPageChange(state.currentPage - 1);
        },
    });

    container.appendChild(prevButton);

    const pageNumbers = getVisiblePageNumbers(state.totalPages, state.currentPage);
    pageNumbers.forEach(pageNumber => {
        const pageButton = buildPageButton({
            label: String(pageNumber + 1),
            className: "page-bubble page-number",
            disabled: pageNumber === state.currentPage,
            active: pageNumber === state.currentPage,
            onClick: () => {
                onPageChange(pageNumber);
            },
        });

        container.appendChild(pageButton);
    });

    const nextButton = buildPageButton({
        label: "Next",
        className: "page-bubble page-nav",
        disabled: state.currentPage >= state.totalPages - 1,
        onClick: () => {
            onPageChange(state.currentPage + 1);
        },
    });

    container.appendChild(nextButton);
}

async function deleteAmzRow(tr, tbody) {
    const rowId = tr.dataset.rowId;

    if (!rowId) {
        tbody.removeChild(tr);
        window.location.reload();
        return;
    }

    try {
        await api.deleteAmzRowById(Number(rowId));
        tbody.removeChild(tr);
        window.location.reload();
    } catch (error) {
        console.error("Failed to delete row:", error);
    }
}

function addRowAmz() {
    const tableBody = document.getElementById("amzTable").getElementsByTagName('tbody')[0];

    let newRow = tableBody.insertRow(-1);

    newRow.dataset.saved = "false";
    
    let cell1 = newRow.insertCell(0);
    let cell2 = newRow.insertCell(1);
    let cell3 = newRow.insertCell(2);
    let cell4 = newRow.insertCell(3);
    let cell5 = newRow.insertCell(4);
    let cell6 = newRow.insertCell(5);

    let indexCell = tableBody.rows.length;
    cell1.textContent = indexCell;

    const dateInput = document.createElement("input");
    dateInput.type = "date";
    cell2.appendChild(dateInput);

    cell3.contentEditable = "true";
    cell3.dataset.placeholder = "# packages?";
    cell3.classList.add("editable", "packages");

    cell4.contentEditable = "false";
    cell4.dataset.placeholder = "$0.00";
    cell4.classList.add("amount");

    cell5.contentEditable = "false";
    cell5.dataset.placeholder = "Who worked?";
    cell5.classList.add("editable", "person");

    const deleteBtn = document.createElement("button");
    deleteBtn.type = "button";
    deleteBtn.textContent = "Delete";
    deleteBtn.classList.add("delete");
    deleteBtn.addEventListener("click", async () => {
        await deleteAmzRow(newRow, tableBody);
        updateAmountForRow(newRow);
    });
    cell6.contentEditable = "false";
    cell6.appendChild(deleteBtn);
        
    

    const personSelect = document.createElement("select");
    personSelect.classList.add("person");

    api.getAllWorkerNames().then(names => {
        names.forEach(name => {
            const option = document.createElement("option");
            option.value = name;
            option.textContent = name;
            personSelect.appendChild(option);
        });
    }).catch(error => {
        console.error("Failed to load worker names:", error);
    });

    cell5.appendChild(personSelect);

    cell3.addEventListener("input", () => {
        updateAmountForRow(newRow);
    });
}

function updateAmountForRow(row) {
    const packagesCell = row.querySelector(".packages");
    const amountCell = row.querySelector(".amount");

    const packages = parseInt(packagesCell.textContent.trim(), 10) || 0;
    const amount = calculateAmount(packages);

    amountCell.textContent = `$${amount.toFixed(2)}`;
    amountCell.dataset.value = amount;
}

function calculateAmount(pkg) {
    const FIRST_TIER_PRICE = 2.0;
    const SECOND_TIER_PRICE = 1.5;
    const THIRD_TIER_PRICE = 1.0;

    let firstTierPackage = Math.min(pkg, 25);
    let secondTierPackage = Math.min(Math.max(pkg - 25, 0), 15);
    let thirdTierPackage = Math.max((pkg - 40), 0);
    
    let total = ((firstTierPackage * FIRST_TIER_PRICE) + (secondTierPackage * SECOND_TIER_PRICE) + (thirdTierPackage * THIRD_TIER_PRICE));
    return total;
}

document.addEventListener("DOMContentLoaded", () => {
    getAllRowsFromDB();
    loadWeeklyTotalsPerPerson();
    loadWeeklyTotal();
});

function renderTransactionRowsPage() {
    const allRows = cachedRows.amzTable;
    const tbody = document.querySelector("#amzTable tbody");

    tbody.innerHTML = "";

    allRows.forEach((row, index) => {
        const tr = tbody.insertRow();
        tr.contentEditable = "false";
        tr.dataset.rowId = row.id;

        const globalRowIndex = (paginationState.amzTable.currentPage * paginationState.amzTable.pageSize) + index + 1;
        tr.insertCell().textContent = globalRowIndex;
        tr.insertCell().textContent = row.dateOfWork;
        tr.insertCell().textContent = row.packageNum;
        tr.insertCell().textContent = row.amount;
        tr.insertCell().textContent = row.person;

        const deleteCell = tr.insertCell();
        deleteCell.contentEditable = "false";

        const deleteBtn = document.createElement("button");
        deleteBtn.type = "button";
        deleteBtn.textContent = "Delete";
        deleteBtn.classList.add("delete");
        deleteBtn.addEventListener("click", async () => {
            await deleteAmzRow(tr, tbody);
        });

        deleteCell.appendChild(deleteBtn);
    });
}

function renderWorkerSummaryPage() {
    const allRows = cachedRows.workerSummaryTable;
    const tbody = document.querySelector("#workerSummaryTable tbody");

    tbody.innerHTML = "";

    allRows.forEach(row => {
        const tr = tbody.insertRow();
        tr.insertCell().textContent = row.weekRange;
        tr.insertCell().textContent = row.worker;
        tr.insertCell().textContent = row.weeklyPackageNumPerPerson;
        tr.insertCell().textContent = row.weeklyAmountPerPerson;
    });
}

function renderWeeklySummaryPage() {
    const allRows = cachedRows.weeklySummaryTable;
    const tbody = document.querySelector("#weeklySummaryTable tbody");

    tbody.innerHTML = "";

    allRows.forEach(row => {
        const tr = tbody.insertRow();
        tr.insertCell().textContent = row.weekRange;
        tr.insertCell().textContent = row.weeklyPackageNum;
        tr.insertCell().textContent = row.weeklyAmount;
    });
}

async function getAllRowsFromDB(page = paginationState.amzTable.currentPage) {
    try {
        const response = await api.getAllRows(page, paginationState.amzTable.pageSize);
        cachedRows.amzTable = response.content;
        syncPaginationState("amzTable", response);
        renderTransactionRowsPage();
        renderPaginationControls("amzTable", getAllRowsFromDB);
    } catch (error) {
        console.error("Failed to load Amazon rows from DB:", error);
    }
}


async function saveProgressAmz() {
    const table = document.getElementById('amzTable');
    const rows = table.querySelectorAll('tbody tr');
    const data = [];

    rows.forEach(row => {
        if(row.dataset.saved === "false") {
            const cells = row.querySelectorAll("td");

            const pkg = parseInt(cells[2].textContent);
            const amount = String(cells[3].textContent).slice(1);

            console.log(data);

            data.push({
                dateOfWork: cells[1].querySelector("input").value,
                amount: amount,
                packageNum: pkg,
                person: cells[4].querySelector("select.person").value,
            });
        }
    });

    if(data.length === 0) {
        console.error("Table row is empty");
        return;
    }

    try {
        await api.saveAmzTable(data);
        rows.forEach(row => row.dataset.saved = "true");
        alert("Progress has been saved properly");
        window.location.reload();
    } catch (error) {
        console.error("There was a problem saving Amazon data:", error);
    }
}


async function loadWeeklyTotalsPerPerson(page = paginationState.workerSummaryTable.currentPage) {
    try {
        const response = await api.getWorkerSummary(page, paginationState.workerSummaryTable.pageSize);
        cachedRows.workerSummaryTable = response.content;
        syncPaginationState("workerSummaryTable", response);
        renderWorkerSummaryPage();
        renderPaginationControls("workerSummaryTable", loadWeeklyTotalsPerPerson);
    } catch (error) {
        console.error("Failed to load worker summary:", error);
    }
}

async function loadWeeklyTotal(page = paginationState.weeklySummaryTable.currentPage) {
    try {
        const response = await api.getMonthlySummary(page, paginationState.weeklySummaryTable.pageSize);
        cachedRows.weeklySummaryTable = response.content;
        syncPaginationState("weeklySummaryTable", response);
        renderWeeklySummaryPage();
        renderPaginationControls("weeklySummaryTable", loadWeeklyTotal);
    } catch (error) {
        console.error("Failed to load monthly summary:", error);
    }
}

window.addRowAmz = addRowAmz;
window.saveProgressAmz = saveProgressAmz;

