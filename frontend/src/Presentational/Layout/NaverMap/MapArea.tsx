import React, { useEffect, useRef, useState } from 'react';
import MapOption from '../../Components/NaverMap/MapOption';
import NaverMap from '../../Components/NaverMap/NaverMap';
import * as MapAreaStyle from './MapArea_Style';
import SidBarArea from './SideBar/SideBarArea';

function MapArea() {
  const { naver } = window;
  const mapElement = useRef(null);
  const [elementIsLoading, setElementIsLoading] = useState(false);
  // eslint-disable-next-line no-undef
  const mapData = useRef<null | naver.maps.Map>(null);
  const initMap = () => {
    if (!mapElement.current || !naver) return;
    if (!naver) return;

    // 지도에 표시할 위치의 위도와 경도 좌표를 파라미터로 넣어줍니다.
    const location = new naver.maps.LatLng(37.5656, 126.9769);
    const map = new naver.maps.Map(mapElement.current, {
      center: location,
      zoom: 8,
      minZoom: 8,
      maxZoom: 15,
      tileSpare: 5,
      logoControl: true,
      logoControlOptions: {
        position: naver.maps.Position.BOTTOM_RIGHT,
      },
      mapDataControl: false,
      zoomControl: true,
      zoomControlOptions: {
        position: naver.maps.Position.RIGHT_CENTER,
        style: naver.maps.ZoomControlStyle.LARGE,
        legendDisabled: true,
      },
      scaleControlOptions: {
        position: naver.maps.Position.BOTTOM_RIGHT,
      },
    });
    mapData.current = map;
    map.zoomBy(1);
  };

  useEffect(() => {
    initMap();
    if (mapData.current) setElementIsLoading(true);
  }, []);

  // const zoomPlus = () => {
  //   mapData.current.setZoom(mapData.current.getZoom() + 1, true);
  // };

  return (
    <MapAreaStyle.MapContainer>
      {/* <MapAreaStyle.TempButton onClick={zoomPlus}>줌인</MapAreaStyle.TempButton> */}
      <SidBarArea />
      <NaverMap ref={mapElement} />
      {elementIsLoading ? <MapOption map={mapData.current} /> : null}
    </MapAreaStyle.MapContainer>
  );
}

export default MapArea;