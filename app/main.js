import React from 'react';
import ReactDOM from 'react-dom';
import {connect, Provider} from 'react-redux';
import {createStore, applyMiddleware} from 'redux';
import thunkMiddleware from 'redux-thunk';
import {createLogger} from 'redux-logger';
import {Navbar, Nav, NavItem, NavDropdown, MenuItem} from 'react-bootstrap';

import UseCaseCardList from  './usecaseList';
import UseCaseForm from './usecaseForm';
import {UseCaseSuiteForm} from './usecaseSuite';
import UseCaseExecutions from './executions';
import futorReducers from './redux/reducer';
import Modal from './comparatorModal';
import {execute} from './network';
import {USER_ACTION} from './constants';


export const store = createStore(futorReducers, applyMiddleware(thunkMiddleware, createLogger()));
store.dispatch(execute(USER_ACTION.RETRIEVE_ALL, null));

class NavBar extends React.Component {
	constructor(props){
		super(props);
		this.state ={activeTab:1};
		this.handleSelect=this.handleSelect.bind(this);
	}
	handleSelect(key) {
	    if(key === 2) {
	        this.props.fetchExecutions();
	    } else if (key === 1) {
	        this.props.fetchUsecases();
	    }

		this.setState({activeTab: key});
	}

	render() {
		let component=<UseCaseCardList/>;
		if(this.state.activeTab == "3.1"){
			component=<UseCaseForm/>;
		} else if(this.state.activeTab == "3.2") {
			component=<UseCaseSuiteForm/>
		} else if(this.state.activeTab ==2 ){
			component=<UseCaseExecutions/>
		}

		return (<div style={{width:"100%"}}><Navbar inverse collapseOnSelect>
            <Navbar.Header>
              <Navbar.Brand>
                <a href="#">FUTIR</a>
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
		{component}
		</div>);
	}
}

const Futor = connect (null, dispatch => {
    return {
        fetchExecutions: () => dispatch(execute(USER_ACTION.RETRIEVE_ALL_EXECUTIONS, null)),
        fetchUsecases: () => dispatch(execute(USER_ACTION.RETRIEVE_ALL, null)),
    }
});

ReactDOM.render(<Provider store={store}>
    <div><Futor/></div>
</Provider>,document.getElementById('navbar'));
