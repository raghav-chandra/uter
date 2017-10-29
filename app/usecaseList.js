import React from 'react';
import {CardHeader,CardFooter, Card,CardBody, Button, CardTitle, CardText, Row, Col,CardSubtitle } from 'reactstrap';
import {USER_ACTION} from './constants'
import {execute} from './network'

export class UseCaseCard extends React.Component{
	constructor(props){
		super(props);
	}

	render() {
		  return (<div class='pull-left' style={{marginRight:'10px'}}>
			<Card body="true" inverse style={{ backgroundColor: '#333', borderColor: '#333',height:'180px',width:'300px' }} >
			        <CardTitle>{this.props.title}
					<div className='pull-right'>
					<Button color="danger"><i class="fa fa-trash-o fa-1"></i></Button>
					<Button color="default"><i class="fa fa-pencil fa-fw fa-1"></i></Button>
					<Button color="success"><i class="fa fa-play fa-1"></i></Button>
					</div>
				</CardTitle>
			        <CardText>{this.props.summary}</CardText>
		        </Card>
		</div>);
	}
}

export class UseCaseCardList extends React.Component{
	constructor(props){
		super(props);
	}
	
	render() {	
		let usecases=execute(USER_ACTION.RETRIEVE_UC,null);
		let data=[];
		usecases.forEach((uc)=>{
			data.push(<UseCaseCard title={uc.ucSummary} summary={uc.ucDescription} input={uc.ucInputJson}/>);
		});
		return (<div class='pull-left'>{data}</div>);
	}
}


