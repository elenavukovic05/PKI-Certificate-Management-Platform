import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActivateCaUser } from './activate-ca-user';

describe('ActivateCaUser', () => {
  let component: ActivateCaUser;
  let fixture: ComponentFixture<ActivateCaUser>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActivateCaUser]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ActivateCaUser);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
