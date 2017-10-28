import React from 'react';
import ReactDOM from 'react-dom';
import { Component } from 'react';
import { UseCaseCardList } from  './usecaseList';
import { UseCase } from './usecaseForm';

import {Navbar,Nav,NavItem,NavDropdown,MenuItem} from 'react-bootstrap';

export default class AppA extends Component {
	constructor(props){
		super(props);
	}
	
	render() {
		return (
		<div>
			<UseCaseCardList />
		</div>
		);
	}
}

class NavBar extends React.Component {
	render() {
		return (<div><Navbar inverse collapseOnSelect>
		<Navbar.Header>
		  <Navbar.Brand>
			<a href="#">UTER</a>
		  </Navbar.Brand>
		  <Navbar.Toggle />
		</Navbar.Header>
		<Navbar.Collapse>
		  <Nav>
			<NavItem eventKey={1} href="#">Link</NavItem>
			<NavItem eventKey={2} href="#">Link</NavItem>
			<NavDropdown eventKey={3} title="Dropdown" id="basic-nav-dropdown">
			  <MenuItem eventKey={3.1}>Action</MenuItem>
			  <MenuItem eventKey={3.2}>Another action</MenuItem>
			  <MenuItem eventKey={3.3}>Something else here</MenuItem>
			  <MenuItem divider />
			  <MenuItem eventKey={3.3}>Separated link</MenuItem>
			</NavDropdown>
		  </Nav>
		  <Nav pullRight>
			<NavItem eventKey={1} href="#">Link Right</NavItem>
		  </Nav>
		</Navbar.Collapse>
	  </Navbar></div>);
	}
}
ReactDOM.render(<NavBar />,document.getElementById('navbar'));

ReactDOM.render(<AppA name="Raghav Chandra"/>,document.getElementById('uter-content'));

//ReactDOM.render(<UseCase/>,document.getElementById('uter-content'));

