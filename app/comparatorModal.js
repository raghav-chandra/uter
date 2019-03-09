import React from 'react';
import {connect} from 'react-redux';

import {Button, Modal, Table} from 'react-bootstrap';
import {launchComparatorModal} from './redux/action';

export class Comparator extends React.Component {
    constructor(props) {
        super(props);
        this.state = {open: false, title: '', data: {}};
    }

    render() {
        let body = [];
        if(this.props.open){
            let data = this.props.data;
            let expected = JSON.parse(data.uc.expected);
            Object.keys(data.matching).forEach(key => {
                let tds = [];
                tds.push(<td><b>{key}</b></td>)
                tds.push(<td><b>{expected[key]}</b></td>);
                tds.push(<td
                    style={data.matching[key] === 'MATCHING' ? {} : {backgroundColor: 'red'}}>{data.actual[key]}</td>);
                body.push(<tr key={key}>{tds}</tr>);
            });
        }

        return (<div>
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
                    <Button onClick={this.handleClose}>Close</Button>
                </Modal.Footer>
        );
    }
}

const mapDispatchToProps = dispatch => {
    return {closePopup: () => dispatch(launchComparatorModal(false, {}))}
}

const mapStateToProps = state => {
    return {
        open: state.launchComparatorModal.open,
        data: state.launchComparatorModal.data
    };
};

export default connect(mapStateToProps, mapDispatchToProps) (Comparator);