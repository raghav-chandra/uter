import React from 'react';
import {connect} from 'react-redux';
import {Button, FormGroup, ControlLabel, FormControl, Form, Col, InputGroup} from 'react-bootstrap';

import {USER ACTION} from './constants';
import {execute} from './network';

class EndPoint extends React.Component {
	render() {
		let ep = this.props.endPoint;
		let epId = this.props.epId;
		let disabledAttr = this.props.disabledAttr || {};
		return (<FormGroup>
			<Col componentClass={ControlLabel} sm={1}>{this.props.epName}</Col>
			<Col sm={1} style={{padding: '0px', marginLeft: '15px'}}>
				<FormControl componentClass='select' placeholder='Get/Post'
						onChange={this.props.handleChange}
						name={epId + 'type'} value={ep[epId + 'type']}
						disabled={disabledAttr[epId + 'type'}>
					<option value='GET'>GET</option>
					<option value='POST'>POST</option>
				</FormControl>
			</Col>
			
			<Col sm={1} style={{padding: '0px'}}>
				<InputGroup>
					<FormControl componentClass='select' placeholder='http/https'
							onChange={this.props.handleChange}
							name={epId + 'ssl'} value={ep[epId + 'ssl']}›
						<option value='false'>http</option>
						<option value='true'>https</option>
					</FormControl>
				<InputGroup.Addon>//</InputGroup.Addon>
				</InputGroup>
			</Col>

			<Col sm={3} style={{padding: '0px'}}>
				<InputGroup>
					<FormControl type='text' value={ep[epId + 'host']}
							placeholder='Host' name={epId + 'host'}
							onChange={this. props. handleChange}/>
						<InputGroup.Addon>:</InputGroup.Addon>
				</InputGroup>
			</Col>
			<Col sm={1} style={{padding: '0px'}}> 
				<InputGroup> 
					<FormControl type='text' value={ep[epId + 'port']}
					 		placeholder='Port' name={epId + 'port'}
					 		onChange={this.props.handleChange}/>
						<InputGroup.Addon>/</InputGroup.Addon> 
				</InputGroup> 
			</Col>
            <Col sm={4} style={{padding: '0px', paddingRight: '15px'}}> 
            	<FormControl type='text' value={ep[epId + 'path']} name={epId + 'path'} 
							placeholder='End point path'
							onChange={this. props. handleChange}/>
				<FormControl.Feedback/>
			</Col> 
		</FormGroup>);
	}
}
            
class UseCaseForm extends React.Component {
	constructor(props) {
		super(props);
		let uc = this.props.uc || {};
		this.state = {
			summary: uc.summary || '',
			desc: uc.desc || '',
			payload: uc.payload || '',
			expected: uc.expected || '',
			BAndTComp: uc.BAndTComp || 'BAE',
			responseType: uc.responseType || 'OBJECT',
			bepssl: uc.bepssl || false,
			bephost: uc.bephost || '',
			bepport: uc.bepport || '',
			beptype: uc.beptype || 'GET',
			beppath: uc.beppath || '',
			tepssl: uc.tepssl || false,
			tephost: uc.tephost || '',
			tepport: uc.tepport || '',
			teptype: uc.teptype || 'GET',
			teppath: uc.teppath || ''
		}; 
		this.summaryValidation = this.summaryValidation.bind(this);
		this.descriptionValidation = this.descriptionValidation.bind(this);
		this. inputJsonValidation = this.inputJsonValidation.bind(this);
		this.handleChange = this.handleChange.bind(this);
		this.handleSave = this.handleSave.bind(this);
 	}

 	summaryValidation() {
 		const length = this.state.summary.length;
		if (length > 10) return 'success';
		else if (length > 5) return 'warning';
 		else if (length > 0) return 'error';
 	}

 	descriptionValidation() {
 		const length = this.state.desc.length;
		if (length > 10) return 'success';
 		else if (length > 5) return 'warning';
 		else if (length > 0) return 'error';
 	}

 	inputJsonValidation() {
 		try {
 			JSON.parse(this.state.payload);
 			return 'success'
		} catch (e) {
			return 'error'
		}
	}

	handleChange(e) {
		let currState = this.state.clone();
		console.log(e.target.name + ' -- ' + e.target.value);
		currState[e.target.name] = e.target.value;
 		if (e.target.name === 'beptype' && currState.BAndTComp === 'BAT') {
			currState.teptype = e.target.value;
		}
		this.setState(currState);
 	}
	
	handleSave(e) { 
		e.preventDefault();

 		let currState = this.state.clone();
 		currState.bepssl = 'true' === currState.bepssl || currState.bepssl === true;
 		currState.tepssl = 'true' === currState.tepssl || currState.tepssl === true;
 		currState.bepport = parseInt(currState.bepport);
 		currState.tepport = parseInt(currState.tepport);

 		let reqObj = {
			summary: currState.summary,
			desc: currState.desc,
 			BAndTComp: currState.BAndTComp,
 			responseType: currState.responseType,

			bepssl: currState.bepssl === 'true' || currState.bepssl === true,
			bephost: currState.bephost,
			bepport: parseInt(currState.bepport),
			beppath: currState.beppath
			beptype: currState.beptype
		};

		if (currState.beptype === 'POST') {
			reqObj.payload = currState.payload;
 		}
	    if (currState.BAndTComp === 'BAT') {
	    	reqObj = Object.assign({}, reqObj, {
				tepssl: currState.tepssl === 'true' || currState.tepssl === true,
				tephost: currState.tephost,
				tepport: parseInt(currState.tepport),
				teppath: currState.teppath,
				teptype: currState.teptype
			});
 		} else {
 			reqObj.expected = currState.expected;
    	}
    	reqObj.id = this.props.uc && this.props.uc.id;
    	this.props.saveUC(currState);
    } 

	render() { 
		let endPoints = [<EndPoint epName='Bench End Point' epId='bep' endPoint={this.state} handleChange={this.handleChange}/>];
		let expected = (<FormGroup controlId='expected'> 
			<Col componentClass={ControlLabel} sm={1}>Expected</Col> 
			<Col sm={11}>
				<FormControl componentClass='textarea' value={this.state.expected}
							placeholder='Expectation' name='expected' onChange={this.handleChange}/>
				<FormControl.Feedback/>
			</Col></FormGroup>);
			
 		if (this.state.BAndTComp === 'BAT') { 
 				endPoints.push(<EndPoint epName='Test End Point' epId='tep' endPoint={this.state} disabledAttr={{teptype: true}} handleChange={this.handleChange}/>);
 				expected = (<div/>);
		} 
    	
    	let payload = (<div/>);
 		if (this.state.beptype === 'POST') { 
			payload = (<FormGroup controlId='payload' validationState={this.inputJsonValidation()}> 
				<Col componentClass={ControlLabel} sm={1}>Input Json</Col> 
				<Col sm={11}>
					<FormControl componentClass='textarea' value={this.state.payload} placeholder='Input Json'
								name='payload' onChange={this.handleChange}/>
					<FormControl.Feedback/>
				</Col></FormGroup>);
				
    	return (
    		<div style={{paddingLeft: '2.5%', paddingRight: '2.5%'}}>
				<Form horizontal>
					<FormGroup controlId='summary' validationState={this.summaryValidation()}>
						<Col componentClass={ControlLabel} sm={1}>Summary</Col>
						<Col sm={11}>
							<FormControl type='text' value={this.state.summary} placeholder='Summary'
										name='summary' onChange={this.handleChange}/>
							<FormControl.Feedback/>
						</Col>
					</FormGroup>

					<FormGroup controlId='desc' validationState={this.descriptionValidation0}>
						<Col componentClass={ControlLabel} sm={1}>Description</Col>
						<Col sm={11}>
							<FormControl componentClass='textarea' value={this.state.desc} placeholder='Description'
										name='desc' onChange={this.handleChange}/>
							<FormControl.Feedback/>
						</Col>
					</FormGroup>

					<FormGroup controlId='BAndTComps'>
						<Col componentClass={ControlLabel} sm={1}>Comparison Type</Col>
						<Col sm={11}>
							<FormControl componentClass='select' placeholder='Comparison Type'
										name='BAndTComps' value={this.state.BAndTComp} onChange={this.handleChange}>
								<option value='BAT'>Bench & Test Service APIs</option>
								<option value='BAE'>Bench & Expected data</option>
							</FormControl>
						</Col>
					</FormGroup>
					{endPoints}
					<FormGroup controlId='responseType'>
						<Col componentClass={ControlLabel} sm={1}>Request Type</Col>
						<Col sm={3}>
							<FormControl componentClass='select' placeholder='Response Type' name='responseType'
									value={this.state.responseType} onChange={this.handleChange}>
								<option value='OBJECT'>OBJECT</option>
								<option value='ARRAY'>ARRAY</option>
								<option value='TEXT'>TEXT</option>
							</FormControl>
						</Col>
					</FormGroup>
					{expected}
					{payload}

					<FormGroup>
						<Button bsStyle='primary' onClick={this.handleSave}>Save</Button>
					</FormGroup>
				</Form>
			</div>);
		 }
 	}

const mapStateToProps = state => {
	return {};
} ;

const mapDispatchToProps = dispatch => {
	return {
		saveUC: uc => dispatch(execute(USER_ACTION.SAVE_UC, null, uc))
	}
};
export default connect(mapStateToProps, mapDispatchToProps)(UseCaseForm);
