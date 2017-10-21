import React from 'react';
import {CardHeader,CardFooter, Card,CardBody, Button, CardTitle, CardText, Row, Col,CardSubtitle } from 'reactstrap';

export class UseCaseCard extends React.Component{
	constructor(props){
		super(props);
	}

	render() {
		  return (<div>
			<Card body="true" inverse style={{ backgroundColor: '#333', borderColor: '#333',height:'180px',width:'300px' }} >
			        <CardTitle>Special Title Treatment <Button color="primary" className="pull-right" >Execute</Button></CardTitle>
			        <CardText>With supporting text below as a natural lead-in to additional content.</CardText>
		        </Card>
		</div>);
	}
}

export class UseCaseCardList extends React.Component{
	constructor(props){
		super(props);
	}
	
	render() {
		let data=[];
			data.push(<UseCaseCard />);
		return (<div>{data}</div>);
	}
}
