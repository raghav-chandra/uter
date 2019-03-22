import React from 'react';
import {connect} from 'react-redux';

import moment from 'moment';
import ReactDataGrid from 'react-data-grid';
import {Toolbar, Editors} from 'react-data-grid-addons';

const columns = [
    {key: 'id', name: 'UC Id'},
    {key: 'summary', name: 'Summary'},
    {key: 'desc', name: 'Description'}
];

class UseCasesResults extends React.Component {
    constructor(props) {
        super(props);
        this.state = {ucs: this.props.useCases, selectedIndexes: [], selectedIds: []}
    }

	componentWillReceiveProps (nextProps) {
		this.setState({ucs: nextProps.useCases, selectedIndexes: [], selectedIds: []});
	}

	onRowsSelected	= rows => {
		this.setState({selectedIndexes: this.state.selectedIndexes.concat(rows.map(r => r.rowIdx)),
			selectedIds: this.state.selectedIds.concat(rows.map(r => r.row.id))});
	};

	onRowsDeselected = rows => {
		let rowIndexes = rows.map(r => r.row.id);
		this.setState({selectedIndexes: this.state.selectedIndexes.filter(i => rowIndexes.indexOf(i) === -1),
			selectedIds: this.state.selectedIds.filter(i => rowIndexes.indexOf(i) === -1)});
	};

    render() {
        if(this.props.fetching) {
            return (<div><h3>Loading UseCases</h3></div>);
        }

        return (<div>
            <ReactDataGrid
				  rowKey="id"
				  columns={columns}
				  rowGetter={(i) => this.state.ucs[i]}
				  rowsCount={this.state.ucs.length}
				  minHeight={500}
				  rowSelection={{
					showCheckbox: true,
					enableShiftSelect: true,
					onRowsSelected: this.onRowsSelected,
					onRowsDeselected: this.onRowsDeselected,
					selectBy: {
					  indexes: this.state.selectedIndexes
					}
				  }}
				/>
        </div>);
    }
}

const mapDispatchToProps = state => {
    return {
        useCases: state.retrieveAll.useCases,
        fetching: state.retrieveAll.fetching
    }
}

export default connect(mapStateToProps, null)(UseCasesResults);

export class UseCaseSuiteForm extends React.Component {
	render() {
		return (<div>Show form to add a new Suite and list to UCs to select </div>);
	}
}