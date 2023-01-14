import { createTheme } from '@mui/material/styles'

const orangeSeaBuckthorn = '#fab023'
const grayAlto = '#cfcfcf'
const grayAltoLight = '#e0e0e0'
const grayScorpion = '#606060'
const grayAlabaster = '#F9F9F9'
const papayaWhip = '#ffefd0'
const evergreen = '#235937'
const redRibbon = '#e40046'

const v5Theme = {
  borderRadius: '2px',
  palette: {
    primary: {
      main: evergreen
    },
    white: '#fff',
    orangeSeaBuckthorn,
    grayAlto,
    grayAltoLight,
    grayScorpion,
    grayAlabaster,
    papayaWhip,
    evergreen,
    redRibbon
  }
}
const theme: any = createTheme(v5Theme)

export default theme
