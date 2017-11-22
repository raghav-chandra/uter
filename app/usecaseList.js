import React from 'react';
import {CardHeader,CardFooter, Card,CardBody, Button,ButtonGroup, CardTitle, CardText, Row, Col,CardSubtitle } from 'reactstrap';
import {USER_ACTION} from './constants'
import {execute} from './network'

const styles = {
	bottomButtons:{
		position:'absolute',
		bottom:0,
		right:0
	},
	topButtons:{
		position:'absolute',
		top:0,
		right:0
	}
};
export class UseCaseCard extends React.Component{
	constructor(props){
		super(props);
	}

	render() {
		  return (<div className='pull-left' style={{marginRight:'10px'}}>
			<Card body="true" inverse style={{ backgroundColor: '#333', borderColor: '#333',height:'180px',width:'300px' }} >
			    <CardTitle>{this.props.title}<Button color="primary" style={styles.topButtons} size='sm'><i className="fa fa-play fa-2"></i></Button>
				</CardTitle>
			    <CardText>{this.props.summary}</CardText>
				<ButtonGroup size='sm'  style={styles.bottomButtons}>
                	<Button color="danger" ><i className="fa fa-trash-o fa-2"></i></Button>
                	<Button color="default" ><i className="fa fa-pencil fa-fw fa-2"></i></Button>
				</ButtonGroup>
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
		return (<div className='pull-left'>{data}</div>);
	}
}


