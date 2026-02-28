import * as api from "./api.js";
import "./theme.js";

function addRowAmz() {
    const tableBody = document.getElementById("amzTable").getElementsByTagName('tbody')[0];

    let newRow = tableBody.insertRow(-1);

    newRow.dataset.saved = "false";
    
    let cell1 = newRow.insertCell(0);
    let cell2 = newRow.insertCell(1);
    let cell3 = newRow.insertCell(2);
    let cell4 = newRow.insertCell(3);
    let cell5 = newRow.insertCell(4);

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

async function getAllRowsFromDB() {
    try {
        const rows = await api.getAllRows();
        const tbody = document.querySelector("#amzTable tbody");

        rows.forEach((row, index) => {
            const tr = tbody.insertRow();
            tr.contentEditable = "true";

            tr.insertCell().textContent = index + 1;
            tr.insertCell().textContent = row.dateOfWork;
            tr.insertCell().textContent = row.packageNum;
            tr.insertCell().textContent = row.amount;
            tr.insertCell().textContent = row.person;
        });
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

            data.push({
                date: cells[1].querySelector("input").value,
                amount: amount,
                package: pkg,
                person: cells[4].querySelector("select.person").value
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
    } catch (error) {
        console.error("There was a problem saving Amazon data:", error);
    }
}


async function loadWeeklyTotalsPerPerson() {
    try {
        const rows = await api.getWorkerSummary();
        const tbody = document.querySelector("#workerSummaryTable tbody");

        rows.forEach(row => {
            const tr = tbody.insertRow();
            tr.insertCell().textContent = row.WeekRange;
            tr.insertCell().textContent = row.Person;
            tr.insertCell().textContent = row.TotalPackages;
            tr.insertCell().textContent = row.TotalAmount;
        });
    } catch (error) {
        console.error("Failed to load worker summary:", error);
    }
}

async function loadWeeklyTotal() {
    try {
        const rows = await api.getMonthlySummary();
        const tbody = document.querySelector("#weeklySummaryTable tbody");

        rows.forEach(row => {
            const tr = tbody.insertRow();
            tr.insertCell().textContent = row.weekRange;
            tr.insertCell().textContent = row.totalPackages;
            tr.insertCell().textContent = row.totalAmount;
        });
    } catch (error) {
        console.error("Failed to load monthly summary:", error);
    }
}

window.addRowAmz = addRowAmz;
window.saveProgressAmz = saveProgressAmz;

