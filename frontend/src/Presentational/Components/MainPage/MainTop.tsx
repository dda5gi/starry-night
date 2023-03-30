import React from 'react';
import * as MainStyle from '../../Page/Main_Style';
import Header from './Header';
import * as TopStyle from './MainTop_Style';

function MainTop() {
  return (
    <MainStyle.Container>
      <Header />
      <TopStyle.MainTop>
        <TopStyle.TopTitle>별 헤는 밤</TopStyle.TopTitle>
        <TopStyle.TopMap to="map">별 보러가기</TopStyle.TopMap>
      </TopStyle.MainTop>
    </MainStyle.Container>
  );
}

export default MainTop;
