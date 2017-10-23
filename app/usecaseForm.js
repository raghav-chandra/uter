import React from 'react'
import {Button,FormGroup,ControlLabel,FormControl,HelpBlock,Form} from 'react-bootstrap'


export class UseCase extends React.Component{
        constructor(props){
                super(props);
                this.state={ucExecutionEndPoint:'',ucResultEndPoint:'',ucSummary:'',ucDescription:''}
                this.summaryValidation=this.summaryValidation.bind(this);
		this.descriptionValidation=this.descriptionValidation.bind(this);
		this.endPointValidation=this.endPointValidation.bind(this);
                this.handleChange=this.handleChange.bind(this);
        }

	summaryValidation() {
		const length = this.state.ucSummary.length;
		if (length > 10) return 'success';
		else if (length > 5) return 'warning';
		else if (length > 0) return 'error';
	}

        descriptionValidation() {
                const length = this.state.ucDescription.length;
                if (length > 10) return 'success';
                else if (length > 5) return 'warning';
                else if (length > 0) return 'error';
        }

	endPointValidation(endPointType) {
		
		let endPoint = this.state[endPointType];
		let len = endPoint.length;
		if (len > 10 && endPoint.startsWith('http')) {
			return 'success';
		} else {
			return 'error';
		}
	}

	handleChange(e) {
		let currState=this.state;
                currState[e.target.id] = e.target.value;
		this.setState(currState);
	}

	render(){
		return (  <Form>
			<FormGroup>
				<ControlLabel>Static text</ControlLabel>
				<FormControl.Static>pnr1122charag3322</FormControl.Static>
			</FormGroup>
			<FormGroup controlId="ucSummary" validationState={this.summaryValidation()}>
				<ControlLabel>Summary</ControlLabel>
				{' '}
				<FormControl type="text" value={this.state.ucSummary} placeholder="Summary" onChange={this.handleChange}/>	
				<FormControl.Feedback />
			</FormGroup>
                        <FormGroup controlId="ucDescription" validationState={this.descriptionValidation()}>
                                <ControlLabel>Description</ControlLabel>
                                {' '}
                                <FormControl componentClass="textarea" value={this.state.ucDescription} placeholder="Description" onChange={this.handleChange}/>
                                <FormControl.Feedback />
                        </FormGroup>
                        <FormGroup controlId="ucExecutionEndPoint" validationState={this.endPointValidation("ucExecutionEndPoint")}>
                                <ControlLabel>Execution End Point</ControlLabel>
                                {' '}
                                <FormControl type="text" value={this.state.ucExecutionEndPoint} placeholder="Execution End point" onChange={this.handleChange}/>
                                <FormControl.Feedback />
                        </FormGroup>
                        <FormGroup controlId="ucResultEndPoint" validationState={this.endPointValidation("ucResultEndPoint")}>
                                <ControlLabel>Result End Point</ControlLabel>
                                {' '}
                                <FormControl type="text" value={this.state.ucResultEndPoint} placeholder="Result End point" onChange={this.handleChange}/>
                                <FormControl.Feedback />
                        </FormGroup>
		</Form>);
	}
}
