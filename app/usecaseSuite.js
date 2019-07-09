import React from 'react';
import {connect} from 'react-redux';

import moment from 'moment';
import ReactDataGrid from 'react-data-grid';
import {Toolbar, Editors} from 'react-data-grid-addons';

import {USER_ACTION} from './constants'

const columns = [
    {key: 'id', name: 'UC Id'},
    {key: 'summary', name: 'Summary'},
    {key: 'desc', name: 'Description'}
];

class UseCasesResults extends React.Component {
    constructor(props) {
        super(props);
        this.state = {ucs: this.props.useCases, selectedIndexes: [], selectedIds: [], searchString: ''};
        this.handleChange = this.handleChange.bind(this);
        this.handleSearch = this.handleSearch.bind(this);
    }

	componentWillReceiveProps (nextProps) {
		this.setState({ucs: nextProps.useCases, selectedIndexes: [], selectedIds: []});
	}

	/*onRowsSelected = rows => {
		this.setState({selectedIndexes: this.state.selectedIndexes.concat(rows.map(r => r.rowIdx)),
			selectedIds: this.state.selectedIds.concat(rows.map(r => r.row.id))});
	};*/

	/*onRowsDeselected = rows => {
		let rowIndexes = rows.map(r => r.row.id);
		this.setState({selectedIndexes: this.state.selectedIndexes.filter(i => rowIndexes.indexOf(i) === -1),
			selectedIds: this.state.selectedIds.filter(i => rowIndexes.indexOf(i) === -1)});
	};*/

	handleChange(e) {
	 	e.preventDefault();
		this.setState({[e.target.name] : e.target.value});
	}

	handleSearch(e)  {
		e.preventDefault();
		this.props.queryUseCases(this.state.searchString);
	}

    render() {
        if(this.props.fetching) {
            return (<div><h3>Loading UseCases</h3></div>);
        }

        return (<div>
        	<div>
        		<Form horizontal>
					<FormGroup controlId='summary' validationState={this.summaryValidation()}>
						<Col sm={11}><FormControl type='text' value={this.state.searchString} placeholder='Search usecases'
							name='searchString' onChange={this.handleChange}/>
						</Col>
						<Col componentClass={ControlLabel} sm={1}>
							<Button bsStyle='primary' onClick={this.handleSearch}>Search</Button>
						</Col>
					</FormGroup>
				</Form>
        	</div>
            <ReactDataGrid
				  rowKey="id"
				  columns={columns}
				  rowGetter={(i) => this.state.ucs[i]}
				  rowsCount={this.state.ucs.length}
				  minHeight={500}
				  rowSelection={{
					showCheckbox: true,
					enableShiftSelect: true,
					onRowsSelected: rows => {
                                    		this.setState({selectedIndexes: this.state.selectedIndexes.concat(rows.map(r => r.rowIdx)),
                                    			selectedIds: this.state.selectedIds.concat(rows.map(r => r.row.id))});
                                    	},
					onRowsDeselected: rows => {
                                      		let rowIndexes = rows.map(r => r.row.id);
                                      		this.setState({selectedIndexes: this.state.selectedIndexes.filter(i => rowIndexes.indexOf(i) === -1),
                                      			selectedIds: this.state.selectedIds.filter(i => rowIndexes.indexOf(i) === -1)});
                                      	},
					selectBy: {
					  indexes: this.state.selectedIndexes
					}
				  }}
				/>
        </div>);
    }
}

const mapStateToProps = state => {
    return {
        useCases: state.retrieveAll.useCases,
        fetching: state.retrieveAll.fetching
    }
}

const mapDispatchToProps = dispatch => {
	return {
		queryUseCases : searchString => dispatch(execute(USER_ACTION.LOOKUP_USECASES, null, {searchString}))
	}
}

export default connect(mapStateToProps, mapDispatchToProps)(UseCasesResults);