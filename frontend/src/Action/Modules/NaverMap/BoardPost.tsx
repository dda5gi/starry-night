/* eslint-disable no-undef */
import axios, { AxiosError } from 'axios';
import PostPinMarker from '../../../Assets/NaverMap/PostPinMarker.png';
// const { naver } = window;

export interface imagesType {
  id: number;
  url: string;
}

export interface writerType {
  id: number;
  name: string;
  profileImageUrl: string;
}

export interface dataType {
  id: number;
  title: string;
  content: string;
  lat: number;
  lng: number;
  writer: writerType;
  images: imagesType[];
  constellationTags: { id: number; name: string }[];
  postLikeCount: number;
  permission: boolean;
  postLikePossible: boolean;
  postLiked: boolean;
  createdDate: Date | string;
}

export interface resultType {
  content: dataType[];
  totalElements: number;
  totalPages: number;
  numberOfElements: number;
  page: number;
  size: number;
}

export interface postCreateType {
  title: string;
  content: string;
  lat: number;
  lng: number;
  constellations: number[];
}

export interface apiResponseType {
  success: boolean;
  message: string;
}

function printError(error: AxiosError) {
  if (error.response) {
    console.log(error.response.data);
  } else if (error.message) {
    console.log('오류 메시지:', error.message);
  } else {
    console.log('알 수 없는 오류가 발생했습니다.');
  }
}

export async function writePost(post: postCreateType, images: File[]): Promise<apiResponseType | null> {
  const formData = new FormData();

  images.forEach((file) => formData.append('images', file));

  let data: apiResponseType | null = null;

  formData.append('post', new Blob([JSON.stringify(post)], { type: 'application/json' }));
  try {
    const res = await axios.post(`${process.env.REACT_APP_API_SERVER_URL}/posts`, formData, {
      withCredentials: true,
    });

    data = res.data as apiResponseType;
  } catch (error) {
    const axiosError = error as AxiosError;

    if (axiosError.response) {
      return axiosError.response.data as apiResponseType;
    }
  }

  return data;
}

export async function GetPostData(maps: naver.maps.Map, page: number, size = 5): Promise<resultType | null> {
  const map = maps;
  const mapBound: naver.maps.Bounds = map.getBounds();

  let data: resultType | null = null;
  try {
    const res = await axios.get(
      `${process.env.REACT_APP_API_SERVER_URL}/posts?pointA=${mapBound.getMax().y}&pointA=${
        mapBound.getMax().x
      }&pointB=${mapBound.getMin().y}&pointB=${mapBound.getMin().x}&page=${page - 1}&size=${size}`,
      { withCredentials: true },
    );
    data = res.data.data as resultType;
  } catch (error) {
    console.log(error);
  }
  return data;
}

export async function MakeMarker(maps: naver.maps.Map, postData: resultType): Promise<naver.maps.Marker[] | null> {
  const markers: naver.maps.Marker[] = [];
  postData.content.forEach((value) => {
    const markerOptions: naver.maps.MarkerOptions = {
      position: new naver.maps.LatLng(value.lat, value.lng),
      map: maps,
      icon: {
        url: PostPinMarker,
        size: new naver.maps.Size(30, 30),
        scaledSize: new naver.maps.Size(30, 30),
      },
      // animation: naver.maps.Animation.DROP,
    };
    markers.push(new naver.maps.Marker(markerOptions));
  });

  return markers;
}

export function clearMarker(markers: naver.maps.Marker[]) {
  if (!markers) return;
  markers.forEach((item) => {
    item.setMap(null);
  });
}

export function changeLikeOn(postId: number) {
  return axios
    .post(`${process.env.REACT_APP_API_SERVER_URL}/posts/${postId}/likes`, null, {
      withCredentials: true,
    })
    .then((res) => res.data)
    .catch((res) => res);
}

export function changeLikeOff(postId: number) {
  return axios
    .delete(`${process.env.REACT_APP_API_SERVER_URL}/posts/${postId}/likes`, { withCredentials: true })
    .then((res) => res.data)
    .catch((res) => res);
}

export function sizeUp(markers: naver.maps.Marker[], idx: number) {
  const prevOption = markers[idx].getOptions();
  markers[idx].setOptions({
    ...prevOption,
    icon: {
      url: PostPinMarker,
      size: new naver.maps.Size(50, 50),
      scaledSize: new naver.maps.Size(50, 50),
    },
  });
}

export function sizeReturn(markers: naver.maps.Marker[], idx: number) {
  const prevOption = markers[idx].getOptions();
  markers[idx].setOptions({
    ...prevOption,
    icon: {
      url: PostPinMarker,
      size: new naver.maps.Size(30, 30),
      scaledSize: new naver.maps.Size(30, 30),
    },
  });
}

export interface ConstellationInfo {
  id: number;
  name: string;
}

// 도전과제 목록 조회
export async function getConstellations(): Promise<ConstellationInfo[]> {
  let data: ConstellationInfo[] = [];

  try {
    const res = await axios.get(`${process.env.REACT_APP_API_SERVER_URL}/constellations`, {
      withCredentials: true,
    });

    data = res.data.data as ConstellationInfo[];
  } catch (error) {
    const axiosError = error as AxiosError;
    printError(axiosError);
  }

  return data;
}
