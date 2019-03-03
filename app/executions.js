import React from 'react';
import {connect} from 'react-redux';

import moment from 'moment';
import ReactDataGrid from 'react-data-grid';
import {Toolbar, Editors} from 'react-data-grid-addons';

import {launchComparatorModal} from './redux/action';
import ComparatorModal from '../comparatorModal';


class ColorFormatter extends React.Component {
    render() {
        const exec = this.props.value;
        let status = exec.finalStatus;
        return (div onClick={e => this.props.showResult(exec)}
                    style={{backgroundColor: status === 'F' ? 'red' : 'lightgreen', textAlign: 'center'}}>
                    <h4>{status}</h4>
        </div>);
    }
}

const ResultFormatter = connect(null, dispatch => {
    return {
        showResult: data => dispatch(launchComparatorModal(true, data));
    }
})(ColorFormatter);

const columns = [
    {key: 'ucId', name: 'UC Id'},
    {key: 'summary', name: 'Summary'},
    {key: 'triggeredAt', name: 'Triggered At'},
    {key: 'status', name: 'Status'},
    {key: 'execution', name: 'Result', formatter: ResultFormatter}
];

const flattenData = rows => {
    rows.forEach(row => {
        row.summary = row.executions.uc.summary;
        row.triggeredAt =  moment.utc(row.epoc).format('YYYY-MM-DD HH:mm:ssZZ');
    });
    return rows;
};

class Executions extends React.Component {
    constructor(props) {
        super(props);
        this.state = {executions: flattenData(this.props.executions || [])};
    }

    render() {
        if(this.props.fetching) {
            return (<div><h3>Loading Executions</h3></div>);
        }

        return (<div>
            <ReactDataGrid
                columns={columns}
                //ref={node => gridInstance = node}
                enableCellSelect={true}
                enableRowSelect={true}
                rowGetter={index => this.state.executions[index]}
                rowCount={this.state.executions.length}
        </div>);
    }
}

const mapStateToProps = state => {
    return {
        executions: state.executions.execs,
        fetching: state.executions.fetching
    };
}

const mapDispatchToProps = dispatch => {
    return {};
};

export default connect(mapStateToProps, mapDispatchToProps)(Executions);