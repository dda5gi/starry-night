import * as React from 'react';
import { motion } from 'framer-motion';

const icon = {
  hidden: {
    opacity: 0,
    pathLength: 0,
    fill: 'rgba(255, 255, 255, 0)',
  },
  visible: {
    opacity: 1,
    pathLength: 1,
    fill: 'rgba(255, 255, 255, 0)',
  },
};

function Leo() {
  return (
    <div className="container">
      <motion.svg xmlns="http://www.w3.org/2000/svg" width="100px" height="100px" viewBox="0 0 700 700">
        <defs>
          <clipPath id="z">
            <path d="M70 0h559.99v559.99H70z" />
          </clipPath>
        </defs>
        <g fill="none" stroke="#000" strokeMiterlimit="10" strokeWidth="41.791" clipPath="url(#z)">
          <motion.path
            fill="currentColor"
            variants={icon}
            initial="hidden"
            animate="visible"
            transition={{
              default: { duration: 2, ease: 'easeInOut' },
              fill: { duration: 2, ease: [1, 0, 0.8, 1] },
            }}
            d="m478.8 369.6c0 14.004-7.4727 26.945-19.602 33.945-12.129 7.0039-27.07 7.0039-39.199 0-12.129-7-19.602-19.941-19.602-33.945-0.003907-13.039 2.0938-25.992 6.2188-38.363l34.27-102.76c8.543-25.617 4.2461-53.777-11.543-75.684-15.785-21.902-41.141-34.883-68.145-34.883-27 0-52.355 12.98-68.145 34.883-15.789 21.906-20.082 50.066-11.543 75.684l1.5664 4.7617v-0.003907c-19.613-6.4297-41.039-4.2305-58.941 6.0547-17.902 10.281-30.594 27.684-34.922 47.867-4.3242 20.188 0.12109 41.262 12.238 57.977 12.113 16.715 30.754 27.504 51.285 29.676 20.527 2.1719 41.016-4.4727 56.359-18.281 15.348-13.812 24.105-33.488 24.102-54.133 0-16.633-2.6836-33.16-7.9531-48.941l-11.871-35.617c-5.1094-15.367-2.5273-32.254 6.9492-45.391 9.4727-13.133 24.68-20.914 40.875-20.914 16.195 0 31.402 7.7812 40.879 20.914 9.4727 13.137 12.059 30.023 6.9453 45.391l-34.27 102.82c-5.2695 15.781-7.957 32.309-7.9531 48.945 0 26.008 13.875 50.039 36.398 63.047 22.523 13.004 50.277 13.004 72.801 0 22.523-13.008 36.398-37.039 36.398-63.047v-11.203h-33.598zm-218.4-28c-10.395 0-20.367-4.1328-27.719-11.484-7.3516-7.3516-11.48-17.32-11.48-27.719 0-10.395 4.1289-20.367 11.48-27.719 7.3516-7.3516 17.324-11.48 27.719-11.48 10.398 0 20.367 4.1289 27.719 11.48 7.3516 7.3516 11.484 17.324 11.484 27.719 0 10.398-4.1328 20.367-11.484 27.719-7.3516 7.3516-17.32 11.484-27.719 11.484z"
          />
        </g>
      </motion.svg>
    </div>
  );
}

export default Leo;
