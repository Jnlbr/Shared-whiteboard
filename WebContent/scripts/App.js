// Variables
var ws;
var username, color;
var posx, posy;

//Utility functions
$ = (id) => {
	return document.getElementById(id);
}
getColor = () => {
	return $('picker').value;
}
getUsername = () => {
	return $('username').value;
}

// Defaults
const canvas = $('canvas');

// Listeners
onMove = (e) => {
	posx = e.pageX - canvas.offsetLeft;
	posy = e.pageY - canvas.offsetTop;
	sendCanvas(posx, posy, color);
}
onOut = (e) => {
	canvas.removeEventListener('mousemove', onMove);
	canvas.removeEventListener('mouseout', onOut);
	sendCanvas(posx, posy, color, false);
}
onDown = (e) => {
	canvas.addEventListener('mousemove', onMove);
	canvas.addEventListener('mouseout', onOut);
}

function clearBoard() {
	c.clearRect(0, 0, 1280, 600)
}

// WEB-SOCKET
function connect() {

	if (ws !== undefined && ws.readyState !== WebSocket.CLOSED) {
		return
	}
	// Init
	color = getColor();
	username = getUsername();
	ws = new WebSocket('ws://localhost:8080/ArrozConLeche/sharedBoard/' + username + '/' + color);
	let cs = canvas.getContext('2d');
	cs.beginPath();

	ws.onopen = (e) => {
		canvas.addEventListener('mouseup', onOut);
		canvas.addEventListener('mousedown', onDown);
	};
	ws.onclose = (e) => {
		if (ws.readyState === WebSocket.OPEN) {
			ws.close();
		}
		canvas.removeEventListener('mouseup', onOut);
		canvas.removeEventListener('mousedown', onDown);
		deleteList();
		console.log('LEAVE')
	};
	ws.onerror = (e) => {
		console.log(e.data);
	};
	ws.onmessage = (e) => {
		let message = JSON.parse(e.data);
		switch (message.event) {
			case 'canvas': {
				let coords = message.coordinates;
				if (coords.isDrawing) {
					cs.strokeStyle = '#' + coords.color;
					cs.lineWidth = 4;
					cs.lineTo(coords.x, coords.y);
					cs.stroke();
				} else {
					cs.closePath();
					cs.beginPath();
				}
				break;
			}			
			case 'join': {
				console.log(message);
				deleteList();
				refreshList(message);
				break;
			}
			case 'leave': {
				console.log(message);
				deleteList();
				refreshList(message);
				break;
			}
			case 'denied': {
				closeWS();
				console.log(message);
				break;
			}
		}
	};

};

function sendCanvas(x, y, color, down = true) {
	let data = {
		event: 'canvas',
		coordinates: {
			isDrawing: down,
			color: color,
			x: x,
			y: y,
		}
	}
	ws.send(JSON.stringify(data));
}

function closeWS() {
	ws.close();
}

refreshList = (message) =>{
	message.players.forEach(player => {
		var li = document.createElement('li');
		li.innerHTML = player.username + `&nbsp;&nbsp;<span style="background-color: #${player.color} ; box-shadow: 0 1px 5px rgba(83, 105, 199, 0.5), 0 1px 15px rgba(0, 0, 0, 0.5); border-radius: 60px;">&nbsp;&nbsp;</span>`;
		$('lista').appendChild(li);
	});
}

deleteList = () =>{
	var lista = $('lista');
	for (let index = 0; index < lista.children.length; index++) {
		lista.removeChild(lista.children[index]);
	}
}