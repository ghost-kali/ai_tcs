import { CircularProgress, Skeleton as MuiSkeleton, Stack, Typography } from '@mui/material'

export function Spinners() {
  return <CircularProgress size={20} />
}

export function Loader({ text }) {
  return (
    <div className='flex justify-center items-center w-full h-[450px]'>
      <div className='flex flex-col items-center gap-3'>
        <CircularProgress color='error' />
        <Typography className='text-slate-800' variant='body2'>
          {text || 'Please wait....'}
        </Typography>
      </div>
    </div>
  )
}

export function Skeleton() {
  return (
    <Stack spacing={1.25} width='100%'>
      <MuiSkeleton variant='text' width='40%' />
      <MuiSkeleton variant='text' width='65%' />
      <MuiSkeleton variant='text' width='55%' />
      <MuiSkeleton variant='rectangular' height={140} />
      <MuiSkeleton variant='text' width='70%' />
      <MuiSkeleton variant='text' width='50%' />
    </Stack>
  )
}

export function Status({ text, icon: Icon, bg, color }) {
  return (
    <div className={`${bg} ${color} px-2 py-2 font-medium rounded-sm flex items-center gap-1`}>
      {text} <Icon size={15} />
    </div>
  )
}

