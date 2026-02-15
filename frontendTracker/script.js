function addRow() {
    const tableBody = document.getElementById("table").getElementsByTagName('tbody')[0];

    let newRow = tableBody.insertRow(-1);

    newRow.dataset.saved = "false"; //set all rows as not saved until their sent into database
    
    let cell1 = newRow.insertCell(0);
    let cell2 = newRow.insertCell(1);
    let cell3 = newRow.insertCell(2);
    let cell4 = newRow.insertCell(3);

    let indexCell = tableBody.rows.length;
    cell1.textContent = indexCell;

    const dateInput = document.createElement("input");
    dateInput.type = "date";
    cell2.appendChild(dateInput);

    cell3.contentEditable = "true";
    cell3.dataset.placeholder = "Enter time (e.g. 02:20)";
    cell3.classList.add("editable");

    cell4.contentEditable = "true";
    cell4.dataset.placeholder = "What did you work on?";
    cell4.classList.add("editable", "description");
}

function saveProgress() {
    const table = document.getElementById('table');
    const rows = table.querySelectorAll('tbody tr');
    const data = [];

    rows.forEach(row => {
        if(row.dataset.saved === "false") {
            const cells = row.querySelectorAll("td");

            time = timeToDecimal(cells[2].textContent);

            data.push({
                date: cells[1].querySelector("input").value,
                hours: time,
                description: cells[3].textContent
            });
        }
    });

    if(data.length > 0) {
        fetch('http://localhost:8080/api/table/saveTable', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data),
        })
        .then(response => {
            if(!response.ok) {
                throw new Error('Network response was not ok: ' + response.statusText);
            }

            rows.forEach(row => row.dataset.saved = "true");
            alert("Progress has been saved properly")
            console.log("save successful");
        })
        .catch(error => {
            console.error('There was a problem with the fetch operation: ', error);
        });
    } else {
        console.error("No information");
    }
}

function timeToDecimal(time) {
    const [h, m] = time.split(":").map(Number);
    return h + m / 60;
}

function decimalToTime(decimal) {
    const hours = Math.floor(decimal);
    const mins = Math.round((decimal - hours) * 60);

    const paddedHours = String(hours).padStart(2, "0");
    const paddedMins = String(mins).padStart(2,"0");

    return `${paddedHours}:${paddedMins}`;
}

document.addEventListener("DOMContentLoaded", loadTableFromDB);

async function loadTableFromDB() {
    const response = await fetch('http://localhost:8080/api/table/loadTable');
    
    if(response === null) return;
    
    const rows = await response.json();
    const tbody = document.querySelector("#table tbody");
    
    rows.forEach((row, index) =>{
        const tr = tbody.insertRow();
        tr.contentEditable = "true";

        displayTime = decimalToTime(row.hours);

        tr.insertCell().textContent = index + 1;
        tr.insertCell().textContent = row.date;
        tr.insertCell().textContent = displayTime;
        tr.insertCell().textContent = row.description;
    });

    getTotalHours();
} 

async function getTotalHours() {
    const response = await fetch('http://localhost:8080/api/table/getTotalHours');

    if(response === null) return;

    const total = await response.json();
    const displayTotal = decimalToTime(total);

    document.getElementById("totalHoursValue").textContent = displayTotal;
    
} 

