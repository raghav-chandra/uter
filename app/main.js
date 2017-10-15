import React from 'react';
import ReactDOM from 'react-dom';
import { Component } from 'react';
import { UseCaseCardList } from  './usecaseList';

export default class AppA extends Component {
constructor(props){
super(props);
}
  render() {
    return (
     <div>
        <h1>Hello World!!! {this.props.name}</h1>
	<UseCaseCardList />
	</div>
      );
  }
}


  ReactDOM.render(<AppA name="Raghav Chandra"/>,
    document.getElementById('root'),
  );
