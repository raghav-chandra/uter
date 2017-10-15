import React from 'react';
import { Card, Button, CardTitle, CardText, Row, Col } from 'reactstrap';

class UseCaseCard extends React.Component{
	constructor(props){
		super(props);
	}

	render() {
		  return (
    <Row>
      <Col sm="6">
        <Card body>
          <CardTitle>Special Title Treatment</CardTitle>
          <CardText>With supporting text below as a natural lead-in to additional content.</CardText>
          <Button>Go somewhere</Button>
        </Card>
      </Col>
      <Col sm="6">
        <Card body>
          <CardTitle>Special Title Treatment</CardTitle>
          <CardText>With supporting text below as a natural lead-in to additional content.</CardText>
          <Button>Go somewhere</Button>
        </Card>
      </Col>
    </Row>
  );
	}
}

export class UseCaseCardList extends React.Component{
	constructor(props){
		super(props);
	}
	
	render() {
		let data=[];
		for(let i=1;i<10;i++){
			data.push(<div><UseCaseCard /></div>);
		}
		return (<div>{data}</div>);
	}
}
