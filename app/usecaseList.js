import React from 'react';
import {CardHeader,CardFooter, Card,CardBody, Button, CardTitle, CardText, Row, Col,CardSubtitle } from 'reactstrap';

export class UseCaseCard extends React.Component{
	constructor(props){
		super(props);
	}

	render() {
		  return (<div>
			<Card body="true" inverse style={{ backgroundColor: '#333', borderColor: '#333',height:'180px',width:'300px' }} >
			        <CardTitle>{this.props.title}<Button color="primary" className="pull-right" >Execute</Button></CardTitle>
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
		let summary='USeCase1'
		let detail='Pull Service data for request and compare results.'
		let data=[];
			data.push(<UseCaseCard title={summary} summary={detail}/>);
		return (<div>{data}</div>);
	}
}


