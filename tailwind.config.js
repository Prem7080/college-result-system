/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        primary: {
          400: '#22d3ee',
          500: '#00d4e8',
          600: '#0891b2',
        },
        navy: {
          800: '#0c1a28',
          900: '#0a1520',
          950: '#07111d',
        }
      },
      fontFamily: {
        syne: ['Syne', 'sans-serif'],
        dm: ['DM Sans', 'sans-serif'],
      }
    },
  },
  plugins: [],
}
