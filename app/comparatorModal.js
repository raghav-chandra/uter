import React from 'react';
import {connect} from 'react-redux';

import {Button, Modal, Table} from 'react-bootstrap';
import {launchComparatorModal} from './redux/action';

export class Comparator extends React.Component {
    constructor(props) {
        super(props);
        this.state = {open: false, title: this.props.title, nestedOpen: false, nestedData: {}};
        this.toggleNested = this.toggleNested.bind(this);
    }

    componentWillReceiveProps(nextProps) {
        this.setState({open: nextProps.open, act: nextProps.act, diff: nextProps.diff, exp: nextProps.exp, title: nextProps.title});
    }

    toggleNested(nestedData) {
        this.setState({nestedData, nestedOpen: !this.state.nestedOpen})
    }

    render() {
        let body = [];
        let comparator = (<div/>);
        if(this.props.open){
            let expected = this.state.exp;
            let diff = this.state.diff;

            if(expected) {
                Object.keys(expected).forEach(key => {
                    let tds = [];
                    tds.push(<td><b>{key}</b></td>)
                    let expObjType = null;
                    if(expected[key] && expected[key].constructor === Array){
                        expObjType = 'Array';
                        tds.push(<td>{expObjType}</td>);
                    } else if (expected[key] && typeof expected[key] === 'object') {
                        expObjType = 'Object';
                        tds.push(<td>{expObjType}</td>);
                    } else {
                        tds.push(<td>{expected[key]}</td>);
                    }

                    let nestedObj = !diff || diff.status === 'P' || diff[key].status === 'P'
                                    ? {act: this.state.act[key], exp: expected[key], diff: {status: 'P'}} : diff[key];
                    nestedObj.title = 'Comparison for : ' + key;

                    tds.push(<td style={!diff || diff.status === 'P' || diff[key].status === 'P' ? {} : {backgroundColor: 'red'}}
                                onClick = {() => expObjType ? this.toggleNested(nestedObj) : null}>
                        {expObjType ? expObjType : this.state.act[key]}</td>);

                    body.push(<tr key={key}>{tds}</tr>);
                });
            }
            comparator = (<Comparator
                                open = {this.state.nestedOpen}
                                act = {this.state.nestedData.act}
                                exp = {this.state.nestedData.exp}
                                diff = {this.state.nestedData.diff}
                                title = {this.state.nestedData.title}
                                closePopup = {() => this.toggleNested({})}/>);
        }

        return (<div>
                {comparator}
                <Modal show={this.props.open} onHide={this.props.closePopup}>
                    <Modal.Header classButton>
                        <Modal.Title>{this.props.title}</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        <Table>
                            <thead style={{fontWeight: 'bold'}}>
                                <tr>
                                    <td>Attributes</td>
                                    <td>Expected</td>
                                    <td>Actual</td>
                                </tr>
                            </thead>
                            <tbody>
                                {body}
                            </tbody>
                        </Table>
                    <Modal.Body>
                    <Modal.Footer>
                        <Button onClick={this.props.closePopup}>Close</Button>
                    </Modal.Footer>
                </Modal>
            </div>);
    }
}

const mapDispatchToProps = dispatch => {
    return {closePopup: () => dispatch(launchComparatorModal(false, {}))}
}

const mapStateToProps = state => {
    return {
        open: state.launchComparatorModal.open,
        diff: state.launchComparatorModal.data.diff,
        act: state.launchComparatorModal.data.act,
        exp: state.launchComparatorModal.data.uc && JSON.parse(state.launchComparatorModal.data.uc.expected),
        title: state.launchComparatorModal.data.uc && state.launchComparatorModal.data.uc.id + ':' + state.launchComparatorModal.data.uc.summary,
    };
};

export default connect(mapStateToProps, mapDispatchToProps) (Comparator);