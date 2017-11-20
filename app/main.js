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
	constructor(props){
		super(props);
		this.state ={activeTab:1};
		this.handleSelect=this.handleSelect.bind(this);
	}
	handleSelect(key) {
		this.setState({activeTab:key});
	}

	render() {
		return (<div style={{width:"100%"}}><Navbar inverse collapseOnSelect>
		<Navbar.Header>
		  <Navbar.Brand>
			<a href="#">UTER</a>
		  </Navbar.Brand>
		  <Navbar.Toggle />
		</Navbar.Header>
		<Navbar.Collapse>
		  <Nav bsStyle="pills" activeKey={this.state.activeTab} onSelect={this.handleSelect}>
			<NavItem eventKey={1} href="#">Executables</NavItem>
			<NavItem eventKey={2} href="#">Executions</NavItem>
			<NavDropdown eventKey="3" title="Add" id="nav-dropdown">
          			<MenuItem eventKey="3.1">Execution</MenuItem>
          			<MenuItem eventKey="3.2">Execution Suite</MenuItem>
          			<MenuItem divider />
          			<MenuItem eventKey="4.4">Separated link</MenuItem>
        		</NavDropdown>
		  </Nav>
		  <Nav pullRight>
			<NavItem eventKey={1} href="#">Link Right</NavItem>
		  </Nav>
		</Navbar.Collapse>
	  </Navbar>

	 <UseCaseCardList />

		</div>);
	}
}
ReactDOM.render(<NavBar />,document.getElementById('navbar'));

//ReactDOM.render(<AppA name="Raghav Chandra"/>,document.getElementById('uter-content'));

//ReactDOM.render(<UseCase/>,document.getElementById('uter-content'));

