import styled from 'styled-components';

const focusColor = '#ed83f7;';

export const MenuWrapper = styled.div`
  margin: 10px 0 10px 0px;
  width: 100%;
  height: 20%;
  border-bottom: 1px solid #ffffffc8;
`;

export const BtnWrapper = styled.div<{ currentSelectedBoard: string }>`
  height: calc(100% / 3);
  align-items: center;
  display: grid;
  grid-template-columns: 20% 80%;
  grid-template-rows: 1fr;

  :hover {
    cursor: pointer;
    background-color: #d4d3d32f;
    transition: 500ms;
    & p {
      color: ${focusColor};
    }
    & .icon {
      color: ${focusColor};
    }
  }

  & .icon {
    color: #b8b8b8;
    margin: auto;
  }

  &.infoBtn {
    ${(props) =>
      props.currentSelectedBoard === 'info'
        ? `
          & p {
            color: ${focusColor};
          }
          & .icon {
            color: ${focusColor};
          }
        `
        : null}
  }
  &.boardBtn {
    ${(props) =>
      props.currentSelectedBoard === 'board'
        ? `
          & p {
            color: ${focusColor};
          }
          & .icon {
            color: ${focusColor};
          }
        `
        : null}
  }
  &.favoriteBtn {
    ${(props) =>
      props.currentSelectedBoard === 'favorite'
        ? `
          & p {
            color: ${focusColor};
          }
          & .icon {
            color: ${focusColor};
          }
        `
        : null}
  }
`;
