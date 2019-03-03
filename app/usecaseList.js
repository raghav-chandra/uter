import React from 'react';
import {connect} from 'react-redux';
import {Card, CardBody, Button, ButtonGroup, CardTitle, CardText} from 'reactstrap';

import {USER_ACTION} from './constants'
import {execute} from './network'

const styles = {
	bottomButtons: {position: 'absolute', bottom: 0, right: 0},
	topButtons: {position: 'absolute', top: 0, right: 0}
};

export class UseCaseCard extends React.Component{
	constructor(props){
		super(props);
		this.execute = this.execute.bind(this);
	}

	execute(e) {
	    e.preventDefault();
	    this.props.executeAction(this.props.uc.id);
	}


	render() {
		  return (<div className='pull-left' style={{marginRight: '10px'}}>
			<Card body="true" inverse
			        style={{backgroundColor: '#333', borderColor: '#333',height:'150px',width:'250px' }} >
			    <CardTitle>{this.props.uc.summary}
			        <Button color="primary" style={styles.topButtons} size='sm' onClick={this.execute}
			                disabled={this.props.executing}><i className="fa fa-play fa-2"></i></Button>
				</CardTitle>
			    <CardText>{this.props.uc.desc}</CardText>
				<ButtonGroup size='sm' style={styles.bottomButtons}>
                	<Button color="danger" disabled={this.props.executing}><i className="fa fa-trash-o fa-2"></i></Button>
                	<Button color="default" disabled={this.props.executing}><i
                	    className="fa fa-pencil fa-fw fa-2"></i></Button>
				</ButtonGroup>
			</Card>
		</div>);
	}
}

const UseCase = connect(state => {
    return {executing: state.ucExecResult.fetching, result: state.ucExecResult.result};
}, dispatch => {
    return {executeAction: id =>dispatch(execute(USER_ACTION.EXECUTE_UC, id))};
})(UseCaseCard);

export class UseCaseCards extends React.Component {
    constructor(props) {
        super(props);
    }

    render () {
        if (this.props.fetching) {
            return (<div><h3>Loading UseCases </h3></div>)
        }

        let data = [];
        this.props.useCases.forEach(uc => data.push(<UseCase uc = {uc}));

        return (<div className = 'pull-left'>{data}</div>);

    }
}

const mapDispatchToProps = state => {
    return {
        useCases: state.retrieveAll.useCases,
        fetching: state.retrieveAll.fetching
    }
}

export default connect(mapDispatchToProps, null)(UsecaseCards);