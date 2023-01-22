import { createTheme } from '@mui/material/styles'

const black = '#000000'
const white = '#fff'
const greenLemon = '#ccff33'
const mint = '#ccffcc'

const v5Theme = {
  borderRadius: '2px',
  palette: {
    primary: {
      main: black
    },
    background: {
      default: mint,
      paper: greenLemon
    }
  }
}
const theme: any = createTheme(v5Theme)

export default theme
