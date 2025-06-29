var encodedLocalMusic = localMusic.map(item => ({
  name: item.name,
  artist: item.artist,
  url: encodeNonAscii(item.url),
  cover: encodeNonAscii(item.cover),
  lrc: encodeNonAscii(item.lrc)
}));

document.getElementById('Music-page').classList.add('localMusic');

function encodeNonAscii(str) {
  return str.replace(/[^\x00-\x7F]/g, function(c) {
    return encodeURIComponent(c);
  });
}

const ap = new APlayer({
  container: document.getElementById('Music-page'),
  lrcType: 3,
  audio: encodedLocalMusic,
  listFolded: window.innerWidth < 768 ? true : false
});

pyj.setupMediaSessionHandlers(ap);