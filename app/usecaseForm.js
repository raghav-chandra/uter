import React from 'react'
import {Button,FormGroup,ControlLabel,FormControl,HelpBlock,Form} from 'react-bootstrap'


export class UseCase extends React.Component{
        constructor(props){
                super(props);
                this.state={value:''}
                this.summaryValidation=this.summaryValidation.bind(this);
		this.descriptionValidation=this.descriptionValidation.bind(this);
                this.handleChange=this.handleChange.bind(this);
        }

	summaryValidation() {
		const length = this.state.value.length;
		if (length > 10) return 'success';
		else if (length > 5) return 'warning';
		else if (length > 0) return 'error';
	}

        descriptionValidation() {
                const length = this.state.value.length;
                if (length > 10) return 'success';
                else if (length > 5) return 'warning';
                else if (length > 0) return 'error';
        }


	handleChange(e) {
		console.log(e.target.name);
		this.setState({ value: e.target.value });
	}

	render(){
		return (  <Form>
			<FormGroup controlId="ucSummary" validationState={this.summaryValidation()}>
				<ControlLabel>Summary</ControlLabel>
				{' '}
				<FormControl type="text" value={this.state.summary} placeholder="Summary" onChange={this.handleChange}/>	
				<FormControl.Feedback />
			</FormGroup>
                        <FormGroup controlId="ucDescription" validationState={this.descriptionValidation()}>
                                <ControlLabel>Description</ControlLabel>
                                {' '}
                                <FormControl componentClass="textarea" placeholder="Description" onChange={this.handleChange}/>
                                <FormControl.Feedback />
                        </FormGroup>

		</Form>);
	}
}
