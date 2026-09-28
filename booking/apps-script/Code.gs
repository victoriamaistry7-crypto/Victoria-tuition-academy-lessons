/**
 * Victoria Tuition Academy — live calendar availability backend
 * One-time setup:
 * 1) Run initialSetup() once and approve Google Calendar access.
 * 2) Deploy as Web app: Execute as Me; Who has access: Anyone.
 * 3) Use the /exec URL in the booking page LIVE_AVAILABILITY_API setting.
 */
const WORK_CALENDAR = 'Victoria Tuition Academy — Work';
const PERSONAL_CALENDAR = 'Victoria — Personal';
const TZ = 'Africa/Johannesburg';

function getOrCreateCalendar_(name, description) {
  const matches = CalendarApp.getCalendarsByName(name);
  if (matches.length) return matches[0];
  return CalendarApp.createCalendar(name, { description: description, timeZone: TZ });
}

function initialSetup() {
  const work = getOrCreateCalendar_(WORK_CALENDAR, 'Confirmed Victoria Tuition Academy lessons and work commitments.');
  const personal = getOrCreateCalendar_(PERSONAL_CALENDAR, 'Private personal commitments. Booking users never see event details.');

  // Move genuine confirmed tutoring lessons from the primary calendar into Work.
  // Prep/admin/study/planning blocks are intentionally NOT migrated.
  const primary = CalendarApp.getDefaultCalendar();
  const start = new Date();
  start.setDate(start.getDate() - 7);
  const end = new Date();
  end.setDate(end.getDate() + 120);

  const workExisting = work.getEvents(start, end).map(e => [
    e.getTitle(), e.getStartTime().getTime(), e.getEndTime().getTime()
  ].join('|'));

  const skip = /(prep|setup|notes|planning|review|admin|marketing|study|lunch|follow[- ]?up|resources|finance|growth|catch[- ]?up|flex block|daily plan|weekly reset|file)/i;

  primary.getEvents(start, end).forEach(e => {
    const title = e.getTitle() || '';
    const desc = e.getDescription() || '';
    const looksLikeLesson = /confirmed/i.test(desc) && /lesson/i.test(desc + ' ' + title);
    if (!looksLikeLesson || skip.test(title)) return;

    const key = [title, e.getStartTime().getTime(), e.getEndTime().getTime()].join('|');
    if (!workExisting.includes(key)) {
      work.createEvent(title, e.getStartTime(), e.getEndTime(), {
        description: desc,
        location: e.getLocation() || ''
      });
    }
    e.deleteEvent();
  });

  return {
    workCalendar: work.getName(),
    personalCalendar: personal.getName(),
    message: 'Setup complete. Put confirmed lessons in Work and private commitments in Personal.'
  };
}

function doGet(e) {
  const callback = safeCallback_(e && e.parameter && e.parameter.callback);
  try {
    const date = String((e && e.parameter && e.parameter.date) || '');
    const duration = Number((e && e.parameter && e.parameter.duration) || 60);
    if (!/^\d{4}-\d{2}-\d{2}$/.test(date) || ![60,120].includes(duration)) {
      return jsonp_(callback, { ok:false, error:'Invalid request' });
    }

    const calendars = findCalendars_();
    if (!calendars.work || !calendars.personal) {
      return jsonp_(callback, {
        ok:false,
        setupRequired:true,
        error:'Work or Personal calendar is missing. Run initialSetup().'
      });
    }

    const dayStart = parseDate_(date, 0, 0);
    const dayEnd = new Date(dayStart);
    dayEnd.setDate(dayEnd.getDate() + 1);

    const busy = [];
    [calendars.work, calendars.personal].forEach(cal => {
      cal.getEvents(dayStart, dayEnd).forEach(ev => {
        if (!ev.isAllDayEvent()) {
          busy.push({
            start: Utilities.formatDate(ev.getStartTime(), TZ, 'HH:mm'),
            end: Utilities.formatDate(ev.getEndTime(), TZ, 'HH:mm')
          });
        } else {
          busy.push({ start:'00:00', end:'23:59' });
        }
      });
    });

    const dow = dayStart.getDay();
    let open = 9 * 60, close = 19 * 60;
    if (dow === 0) { open = 13 * 60; close = 18 * 60; }

    const slots = [];
    for (let s = open; s + duration <= close; s += 30) {
      const end = s + duration;
      const available = !busy.some(b => s < mins_(b.end) && end > mins_(b.start));
      slots.push({ time: hhmm_(s), available: available });
    }

    return jsonp_(callback, {
      ok:true,
      date:date,
      duration:duration,
      slots:slots,
      syncedAt:new Date().toISOString()
    });
  } catch (err) {
    return jsonp_(callback, { ok:false, error:String(err && err.message || err) });
  }
}

function findCalendars_() {
  const work = CalendarApp.getCalendarsByName(WORK_CALENDAR)[0] || null;
  const personal = CalendarApp.getCalendarsByName(PERSONAL_CALENDAR)[0] || null;
  return { work:work, personal:personal };
}

function parseDate_(iso, hour, minute) {
  const p = iso.split('-').map(Number);
  return new Date(p[0], p[1]-1, p[2], hour, minute, 0, 0);
}
function mins_(t) { const p=t.split(':').map(Number); return p[0]*60+p[1]; }
function hhmm_(m) { return ('0'+Math.floor(m/60)).slice(-2)+':'+('0'+(m%60)).slice(-2); }
function safeCallback_(name) {
  return /^[A-Za-z_$][0-9A-Za-z_$\.]*$/.test(name || '') ? name : 'vtaAvailabilityCallback';
}
function jsonp_(callback, obj) {
  return ContentService
    .createTextOutput(callback + '(' + JSON.stringify(obj) + ');')
    .setMimeType(ContentService.MimeType.JAVASCRIPT);
}
