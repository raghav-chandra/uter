import React from 'react';
import {connect} from 'react-redux';
import {Card, CardBody, Button, ButtonGroup, CardTitle, CardText} from 'reactstrap';

import {USER_ACTION} from './constants';
import {execute} from './network';
import {editUC} from './redux/action';

const styles = {
	bottomButtons: {position: 'absolute', bottom: 0, right: 0},
	topButtons: {position: 'absolute', top: 0, right: 0}
};

export class UseCaseCard extends React.Component{
	constructor(props){
		super(props);
		this.execute = this.execute.bind(this);
		this.edit = this.edit.bind(this);
	}

	execute(e) {
	    e.preventDefault();
	    this.props.executeAction(this.props.uc.id);
	}

	edit(e) {
		e.preventDefault();
		this.props.launchEdit(this.props.uc);
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
                	<Button color="default" disabled={this.props.executing} onClick={this.edit}><i
                	    className="fa fa-pencil fa-fw fa-2"></i></Button>
				</ButtonGroup>
			</Card>
		</div>);
	}
}

const UseCase = connect(state => {
    return {executing: state.ucExecResult.fetching, result: state.ucExecResult.result};
}, dispatch => {
    return {
    	executeAction: id => dispatch(execute(USER_ACTION.EXECUTE_UC, id)),
    	launchEdit: uc => dispatch(editUC(true, uc))
	}
})(UseCaseCard);

export class UseCaseCards extends React.Component {
    constructor(props) {
        super(props);
    }

    render () {
        if (this.props.fetching) {
            return (<div><h3>Loading UseCases </h3></div>)
        }

        let data = [<h3>UseCases</h3>];
        this.props.useCases.forEach(uc => data.push(<UseCase uc = {uc} />));
        /*data.push(<h3>UseCases Suites</h3>);
        this.props.useCases.forEach(uc => data.push(<UseCase uc = {uc} />));*/

        return (<div className = 'pull-left'>{data}</div>);

    }
}

const mapStateToProps = state => {
    return {
        useCases: state.retrieveAll.useCases,
        fetching: state.retrieveAll.fetching
    }
}

export default connect(mapStateToProps, null)(UseCaseCards);